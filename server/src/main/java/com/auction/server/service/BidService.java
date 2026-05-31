package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.UserDAO;
import javax.sql.DataSource;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.exceptions.ConcurrentBidException;
import com.auction.share.exceptions.InsufficientBalanceException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.auction.BidTransaction;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.User;
import java.util.Set;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class BidService implements IBidService {
    private final DataSource dataSource;
    private final AuctionDAO auctionDAO;
    private final BidTransactionDAO bidTransactionDAO;
    private final UserDAO userDAO;
    private final BroadcastService bidBroadcastService;


    public BidService(
            DataSource dataSource,
            AuctionDAO auctionDAO,
            BidTransactionDAO bidTransactionDAO,
            UserDAO userDAO,
            BroadcastService bidBroadcastService) {
        this.dataSource = dataSource;
        this.auctionDAO = auctionDAO;
        this.bidTransactionDAO = bidTransactionDAO;
        this.userDAO = userDAO;
        this.bidBroadcastService = bidBroadcastService;
    }



    public boolean placeBid(PlaceBidRequest req) throws SQLException, ValidationException {
        try (Connection conn = dataSource.getConnection()) {
            Auction auction = auctionDAO.findById(conn, req.getAuctionId());
            if (auction == null || !auction.isRunning()) {
                throw new ValidationException(
                        auction == null ? "Auction not found." : "Auction is not running.");
            }

            Bidder bidder = requireBidder(conn, req.getBidderId());
            if (req.getAmount() <= auction.getCurrentHighestBid()) {
                throw new ConcurrentBidException("Bid amount must be higher than current highest bid.");
            }
            if (req.getAmount() < auction.getCurrentHighestBid() + auction.getBidStep()) {
                throw new ConcurrentBidException("Bid amount must be at least current price + bid step.");
            }

            BidTransaction transaction = new BidTransaction(auction, bidder, req.getAmount());

            conn.setAutoCommit(false);
            try {
                // 1. Lock user balance row to prevent double-spending
                double currentBalance = userDAO.findBalanceForUpdate(conn, bidder.getId());

                // 2. Check if user has enough balance considering reserved amounts in other running auctions
                double reservedInOtherRunningAuctions = auctionDAO.sumAuctionCurrentPrices(conn, bidder.getId(), auction.getId());
                double requiredForThisBid = req.getAmount();
                if (auction.getHighestBidder() != null && bidder.getId().equals(auction.getHighestBidder().getId())) {
                    requiredForThisBid = req.getAmount() - auction.getCurrentHighestBid();
                }

                if (currentBalance - reservedInOtherRunningAuctions < requiredForThisBid) {
                    throw new InsufficientBalanceException("Insufficient balance.");
                }

                // 3. Atomic check & update auction
                boolean updated = auctionDAO.updateHighestBid(conn, auction.getId(), bidder.getId(), req.getAmount());
                if (!updated) {
                    throw new ConcurrentBidException(
                            "Bid rejected: auction is not running, already ended, or current price changed.");
                }

                bidTransactionDAO.saveBidTransaction(conn, transaction);

                // 4. Re-fetch auction within the same transaction to get the exact bid_count and end_time
                Auction updatedAuction = auctionDAO.findById(conn, auction.getId());

                conn.commit();

                // 5. Broadcast the new event
                bidBroadcastService.broadcastBidUpdate(
                        new BidUpdateEvent(
                                BroadcastService.BID_UPDATED,
                                updatedAuction.getId(),
                                bidder.getId(),
                                bidder.getFullName(),
                                req.getAmount(),
                                req.getAmount(),
                                transaction.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                updatedAuction.getBidCount()));
            } catch (SQLException | ValidationException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            return true;
        }
    }


    private Bidder requireBidder(Connection conn, String bidderId) throws SQLException, ValidationException {
        User bidderUser = userDAO.findById(conn, bidderId);
        if (!(bidderUser instanceof Bidder bidder)) {
            throw new ValidationException("User is not a bidder.");
        }
        return bidder;
    }


}
