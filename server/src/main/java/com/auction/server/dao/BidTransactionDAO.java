package com.auction.server.dao;

import com.auction.server.util.DatabaseConnection;
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

}
