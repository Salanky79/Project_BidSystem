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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BidTransactionDAO {
    private final UserDAO userDAO = new UserDAO();

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

    // tìm lịch suer giao dịch của 1 phiên đấu giá ( theo ID, TimeStamps )
    public List<BidTransaction> findByAuction(Auction auction) throws SQLException {
        List<BidTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM bid_transactions WHERE auction_id = ? ORDER BY timestamp DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, auction.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String bidderId = rs.getString("bidder_id");
                    double amount = rs.getDouble("amount");
                    Timestamp ts = rs.getTimestamp("timestamp");
                    
                    User user = userDAO.findById(bidderId);
                    if (user instanceof Bidder bidder) {
                        BidTransaction bt = new BidTransaction(auction, bidder, amount);
                        bt.setID(rs.getString("id"));
                        if (ts != null) {
                            bt.setTimestamp(ts.toLocalDateTime());
                        }
                        list.add(bt);
                    }
                }
            }
        }
        return list;
    }

    // xem lịch sử tất cả giao dịch của ai đó ( join : bid_transactions + auctions + items )
    public List<ProfileBidTransactionDTO> findProfileTransactionsByBidderId(String bidderId) throws SQLException {
        List<ProfileBidTransactionDTO> list = new ArrayList<>();
        String sql = """
                SELECT i.name AS item_name, a.status, bt.amount, bt.timestamp
                FROM bid_transactions bt
                JOIN auctions a ON a.id = bt.auction_id
                JOIN items i ON i.id = a.item_id
                WHERE bt.bidder_id = ?
                ORDER BY bt.timestamp DESC
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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

    /**
     * Đếm số lượt đặt giá cho nhiều auction cùng lúc bằng 1 query GROUP BY.
     * Tránh N+1 queries khi hiển thị danh sách auction.
     *
     * @param auctionIds danh sách ID auction cần đếm
     * @return Map: auctionId → số lượt đặt giá
     */
    // HashMap ( auction - countBid )
    public java.util.Map<String, Integer> countByAuctionIds(java.util.List<String> auctionIds) throws SQLException {
        java.util.Map<String, Integer> result = new java.util.HashMap<>();
        if (auctionIds == null || auctionIds.isEmpty()) {
            return result;
        }

        String placeholders = "?,".repeat(auctionIds.size());
        placeholders = placeholders.substring(0, placeholders.length() - 1);
        String sql = "SELECT auction_id, COUNT(*) AS cnt FROM bid_transactions WHERE auction_id IN ("
                + placeholders + ") GROUP BY auction_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < auctionIds.size(); i++) {
                ps.setString(i + 1, auctionIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("auction_id"), rs.getInt("cnt"));
                }
            }
        }
        return result;
    }
}
