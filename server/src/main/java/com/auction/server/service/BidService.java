package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.util.DatabaseConnection;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.auction.BidTransaction;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BidService {
    private final AuctionDAO auctionDAO;
    private final BidTransactionDAO bidTransactionDAO;
    private final UserDAO userDAO;
    private final BidBroadcastService bidBroadcastService;
    private AutoBidService autoBidService;

    public BidService(
            AuctionDAO auctionDAO,
            BidTransactionDAO bidTransactionDAO,
            UserDAO userDAO,
            BidBroadcastService bidBroadcastService) {
        this.auctionDAO = auctionDAO;
        this.bidTransactionDAO = bidTransactionDAO;
        this.userDAO = userDAO;
        this.bidBroadcastService = bidBroadcastService;
    }

    public void setAutoBidService(AutoBidService autoBidService) {
        this.autoBidService = autoBidService;
    }

    public boolean autoPlaceBid(PlaceBidRequest req) throws SQLException, ValidationException {
        return placeBid(req, true);
    }

    public boolean placeBid(PlaceBidRequest req, boolean triggerAutoBid) throws SQLException, ValidationException {
        Auction auction = auctionDAO.findById(req.getAuctionId());
        if (!isAuctionRunning(auction)) {
            throw new ValidationException(
                    auction == null ? "Auction not found." : "Auction is not running.");
        }

        Bidder bidder = requireBidder(req.getBidderId());
        if (req.getAmount() <= auction.getCurrentHighestBid()) {
            throw new ValidationException("Bid amount must be higher than current highest bid.");
        }
        if (req.getAmount() < auction.getCurrentHighestBid() + auction.getBidStep()) {
            throw new ValidationException("Bid amount must be at least current price + bid step.");
        }

        placeBidAndBroadcast(auction, bidder, req.getAmount());
        if (triggerAutoBid && autoBidService != null) {
            autoBidService.triggerAutoBid(req.getAuctionId(), bidder.getId());
        }
        return true;
    }

    private void placeBidAndBroadcast(Auction auction, Bidder bidder, double amount) throws SQLException, ValidationException {
        BidTransaction transaction = new BidTransaction(auction, bidder, amount);

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean updated = auctionDAO.updateHighestBidIfHigher(conn, auction.getId(), bidder.getId(), amount);
                if (!updated) {
                    conn.rollback();
                    throw new ValidationException(
                            "Bid rejected: auction is not running, already ended, insufficient balance, or current price changed.");
                }

                bidTransactionDAO.saveBidTransaction(conn, transaction);
                conn.commit();
                bidBroadcastService.broadcastBidUpdate(
                        new BidUpdateEvent(
                                BidBroadcastService.BID_UPDATED,
                                auction.getId(),
                                bidder.getId(),
                                bidder.getFullName(),
                                amount,
                                amount,
                                transaction.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            } catch (SQLException | ValidationException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private Bidder requireBidder(String bidderId) throws SQLException, ValidationException {
        User bidderUser = userDAO.findById(bidderId);
        if (!(bidderUser instanceof Bidder bidder)) {
            throw new ValidationException("User is not a bidder.");
        }
        return bidder;
    }

    private boolean isAuctionRunning(Auction auction) {
        if (auction == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return !(now.isAfter(auction.getEndTime()) || now.isBefore(auction.getStartTime()));
    }
}
