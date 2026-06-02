package com.auction.server.dao;

import com.auction.server.util.DatabaseConnection;
import com.auction.share.DTO.ProfileBidTransactionDTO;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.auction.BidTransaction;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BidTransactionDAO  {
    public BidTransactionDAO() {
    }

    public boolean saveBidTransaction(Connection conn, BidTransaction transaction) throws SQLException {
        String sql = "INSERT INTO bid_transactions (id, auction_id, bidder_id, amount, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transaction.getId());
            ps.setString(2, transaction.getAuction().getId());
            ps.setString(3, transaction.getBidder().getId());
            ps.setDouble(4, transaction.getAmount());
            ps.setTimestamp(5, Timestamp.valueOf(transaction.getTimestamp()));
            return ps.executeUpdate() > 0;
        }
    }

    public List<BidTransaction> findByAuction(Connection conn, Auction auction) throws SQLException {
        List<BidTransaction> list = new ArrayList<>();
        String sql = """
                SELECT bt.*, u.username, u.fullname, u.email, u.balance, u.phoneNumber, u.address
                FROM bid_transactions bt
                JOIN users u ON u.id = bt.bidder_id
                WHERE bt.auction_id = ?
                ORDER BY bt.timestamp DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, auction.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String bidderId = rs.getString("bidder_id");
                    double amount = rs.getDouble("amount");
                    Timestamp ts = rs.getTimestamp("timestamp");
                    
                    Bidder bidder = new Bidder(
                            rs.getString("username"),
                            null,
                            rs.getString("fullname"),
                            rs.getString("phoneNumber"),
                            rs.getString("email"),
                            rs.getString("address")
                    );
                    bidder.setBalance(rs.getDouble("balance"));
                    bidder.setID(bidderId);

                    BidTransaction bt = new BidTransaction(auction, bidder, amount);
                    bt.setID(rs.getString("id"));
                    if (ts != null) {
                        bt.setTimestamp(ts.toLocalDateTime());
                    }
                    list.add(bt);
                }
            }
        }
        return list;
    }

    public List<ProfileBidTransactionDTO> findProfileTransactionsByBidderId(Connection conn, String bidderId) throws SQLException {
        List<ProfileBidTransactionDTO> list = new ArrayList<>();
        String sql = """
                SELECT i.name AS item_name, a.status, bt.amount, bt.timestamp
                FROM bid_transactions bt
                JOIN auctions a ON a.id = bt.auction_id
                JOIN items i ON i.id = a.item_id
                WHERE bt.bidder_id = ?
                ORDER BY bt.timestamp DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bidderId);
            try (ResultSet rs = ps.executeQuery()) {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("timestamp");
                    String timestamp = ts == null ? "" : ts.toLocalDateTime().format(formatter);
                    list.add(new ProfileBidTransactionDTO(
                            rs.getString("item_name"),
                            rs.getString("status"),
                            rs.getDouble("amount"),
                            timestamp
                    ));
                }
            }
        }
        return list;
    }
}

