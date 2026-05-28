package com.auction.server.dao;

import com.auction.share.enums.AuctionStatus;
import com.auction.share.models.auction.Auction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface AuctionDAO {
    boolean saveAuction(Connection conn, Auction auction) throws SQLException;
    boolean updateStatus(Connection conn, String id, AuctionStatus status) throws SQLException;
    
    Auction findById(Connection conn, String id) throws SQLException;
    
    List<Auction> findAll(Connection conn) throws SQLException;
    List<Auction> findByStatus(Connection conn, AuctionStatus status) throws SQLException;
    List<Auction> findBySeller(Connection conn, String sellerId) throws SQLException;
    List<Auction> findBySellerAndStatus(Connection conn, String sellerId, AuctionStatus status) throws SQLException;
    
    double sumAuctionCurrentPrices(Connection conn, String bidderId, Set<String> excludedAuctionIds) throws SQLException;
    
    boolean updateHighestBid(Connection conn, String id, String bidderId, double amount) throws SQLException;
    
    boolean updateBidStep(Connection conn, String auctionId, double step) throws SQLException;
    
    int markOpenAuctionsAsRunning(Connection conn) throws SQLException;
    int finishAuctions(Connection conn, Timestamp endTime) throws SQLException;
    
    List<String> findEndedRunningAuctionIds(Connection conn, LocalDateTime now) throws SQLException;
}
