package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.factory.ItemFactory;
import com.auction.server.util.DatabaseConnection;
import com.auction.share.DTO.*;
import com.auction.share.enums.AuctionStatus;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.auction.BidTransaction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;

import java.sql.SQLException;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;


public class AuctionService {
    private final AuctionDAO auctionDAO;
    private final ItemDAO itemDAO;
    private final BidTransactionDAO bidTransactionDAO;
    private final UserDAO userDAO;
    private final BidBroadcastService bidBroadcastService;

    public AuctionService(
            AuctionDAO auctionDAO,
            ItemDAO itemDAO,
            BidTransactionDAO bidTransactionDAO,
            UserDAO userDAO,
            BidBroadcastService bidBroadcastService
    ) {
        this.auctionDAO = auctionDAO;
        this.itemDAO = itemDAO;
        this.bidTransactionDAO = bidTransactionDAO;
        this.userDAO = userDAO;
        this.bidBroadcastService = bidBroadcastService;
    }

    public Auction createAuction(CreateAuctionRequest req) throws SQLException, ValidationException {
        User sellerUser = userDAO.findById(req.getSellerId());
        if (!(sellerUser instanceof Seller seller)) {
            throw new ValidationException("User is not a seller.");
        }

        LocalDateTime startTime = parseDateTime(req.getStartTime());
        LocalDateTime endTime = parseDateTime(req.getEndTime());
        if (!startTime.isBefore(endTime)) {
            throw new ValidationException("Start time must be before end time.");
        }

        Item item = createItemFromCategory(req);
        itemDAO.saveItem(item);

        Auction auction = new Auction(item, seller, startTime, endTime);

        auctionDAO.saveAuction(auction);
        return auction;
    }

    public boolean placeBid(PlaceBidRequest req) throws SQLException, ValidationException {
        Auction auction = auctionDAO.findById(req.getAuctionId());
        if (auction == null) {
            throw new ValidationException("Auction not found.");
        }

        if (LocalDateTime.now().isAfter(auction.getEndTime()) || LocalDateTime.now().isBefore(auction.getStartTime())){
            throw new ValidationException("Auction is not running.");
        }

        User bidderUser = userDAO.findById(req.getBidderId());
        if (!(bidderUser instanceof Bidder bidder)) {
            throw new ValidationException("User is not a bidder.");
        }

        if (req.getAmount() <= auction.getCurrentHighestBid()) {
            throw new ValidationException("Bid amount must be higher than current highest bid.");
        }

        BidTransaction transaction = new BidTransaction(auction, bidder, req.getAmount());

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean updated = auctionDAO.updateHighestBidIfHigher(conn, auction.getId(), bidder.getId(), req.getAmount());
                if (!updated) {
                    conn.rollback();
                    throw new ValidationException("Bid rejected: auction is not running, already ended, insufficient balance, or current price changed.");
                }

                bidTransactionDAO.saveBidTransaction(conn, transaction);
                conn.commit();
                // Gửi thông báo "có bid mới" đến tất cả client đang xem auction đó
                bidBroadcastService.broadcastBidUpdate(new BidUpdateEvent(
                        BidBroadcastService.BID_UPDATED,
                        auction.getId(),
                        bidder.getId(),
                        bidder.getFullName(),
                        req.getAmount(),
                        req.getAmount(),
                        transaction.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        // chuyen datetime thanh chuoi chuan theo format ngay thang
                ));
                return true;
            } catch (SQLException | ValidationException e) {
                conn.rollback(); // huy toan bi thay doi
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public AuctionDetailDTO getAuctionDetail(String auctionId) throws SQLException, ValidationException {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) {
            throw new ValidationException("Auction not found.");
        }

        List<BidTransaction> transactions = bidTransactionDAO.findByAuction(auction);
        List<BidDTO> bidHistory = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        for (BidTransaction tx : transactions) {
            bidHistory.add(new BidDTO(
                tx.getBidder().getFullName(),
                tx.getAmount(),
                tx.getTimestamp().format(formatter)
            ));
        }

        String highestBidderName = auction.getHighestBidder() != null ? auction.getHighestBidder().getFullName() : null;

        return new AuctionDetailDTO(
            auction.getId(),
            auction.getItem().getName(),
            auction.getItem().getDescription(),
            auction.getItem().getCategory().name(),
            auction.getSeller().getFullName(),
            auction.getItem().getStartingPrice(),
            auction.getCurrentHighestBid(),
            auction.getStatus().name(),
            auction.getStartTime().format(formatter),
            auction.getEndTime().format(formatter),
            highestBidderName,
            bidHistory
        );
    }

    public List<AuctionSummaryDTO> listAuctions(ListAuctionRequest req) throws SQLException {
        List<Auction> auctions = resolveAuctionsByFilter(req);
        List<AuctionSummaryDTO> summaries = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        for (Auction auction : auctions) {
            summaries.add(new AuctionSummaryDTO(
                auction.getId(),
                auction.getItem().getName(),
                auction.getItem().getCategory().name(),
                auction.getCurrentHighestBid(),
                auction.getStatus().name(),
                auction.getEndTime().format(formatter)
            ));
        }
        return summaries;
    }


    // HELPER
    private LocalDateTime parseDateTime(String dateTimeStr) throws ValidationException {
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Please use ISO format.");
        }
    }

    private Item createItemFromCategory(CreateAuctionRequest req) throws ValidationException {
        String category = req.getCategory();

        try {
            return ItemFactory.createItem(
                    category,
                    req.getItemName(),
                    req.getDescription(),
                    req.getStartingPrice(),
                    req.getSellerId(),
                    req.getAttributes()
            );
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unsupported item category: " + category);
        }
    }

    private List<Auction> resolveAuctionsByFilter(ListAuctionRequest req) throws SQLException {
        if (req == null || req.getStatus() == null || req.getStatus().isBlank()) {
            return auctionDAO.findAll();
        }

        try {
            AuctionStatus status = AuctionStatus.valueOf(req.getStatus().trim().toUpperCase());
            return auctionDAO.findByStatus(status);
        } catch (IllegalArgumentException e) {
            return auctionDAO.findAll();
        }
    }
}
