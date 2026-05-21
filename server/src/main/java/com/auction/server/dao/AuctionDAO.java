package com.auction.server.dao;

import com.auction.server.util.DatabaseConnection;
import com.auction.server.util.MapAuctionDB;
import com.auction.share.enums.AuctionStatus;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {
    private final ItemDAO itemDAO = new ItemDAO();
    private final UserDAO userDAO = new UserDAO();

    public boolean saveAuction(Auction auction) throws SQLException {
        String sql = "INSERT INTO auctions (id, item_id, seller_id, current_price, highest_bidder_id, start_time, end_time, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, auction.getId());
            ps.setString(2, auction.getItem().getId());
            ps.setString(3, auction.getSeller().getId());
            ps.setDouble(4, auction.getCurrentHighestBid());
            if (auction.getHighestBidder() != null) {
                ps.setString(5, auction.getHighestBidder().getId());
            } else {
                ps.setNull(5, java.sql.Types.VARCHAR);
            }
            ps.setTimestamp(6, Timestamp.valueOf(auction.getStartTime()));
            ps.setTimestamp(7, Timestamp.valueOf(auction.getEndTime()));
            ps.setString(8, auction.getStatus().name());
            
            int row = ps.executeUpdate();
            return row > 0;
        }
    }

    public Auction findById(String id) throws SQLException {
        String sql = "SELECT * FROM auctions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractAuction(rs);
                }
            }
        }
        return null;
    }

    public List<Auction> findAll() throws SQLException {
        List<Auction> list = new ArrayList<>();
        String sql = "SELECT * FROM auctions ORDER BY start_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                Auction auction = extractAuction(rs);
                if (auction != null) {
                    list.add(auction);
                }
            }
        }
        return list;
    }

    public List<Auction> findByStatus(AuctionStatus status) throws SQLException {
        List<Auction> list = new ArrayList<>();
        String sql = "SELECT * FROM auctions WHERE status = ? ORDER BY start_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Auction auction = extractAuction(rs);
                    if (auction != null) {
                        list.add(auction);
                    }
                }
            }
        }
        return list;
    }


    public boolean updateHighestBidIfHigher(Connection conn, String id, String bidderId, double amount) throws SQLException {
        String sql = """
                UPDATE auctions a
                JOIN users u ON u.id = ?
                SET a.current_price = ?,
                    a.highest_bidder_id = ?,
                    a.end_time = CASE
                        WHEN TIMESTAMPDIFF(SECOND, ?, a.end_time) <= 10
                             AND TIMESTAMPDIFF(SECOND, ?, a.end_time) >= 0
                        THEN DATE_ADD(a.end_time, INTERVAL 30 SECOND)
                        ELSE a.end_time
                    END
                WHERE a.id = ?
                  AND a.status = ?
                  AND a.start_time <= ?
                  AND a.end_time > ?
                  AND a.current_price < ?
                  AND (
                    u.balance - (
                      SELECT COALESCE(SUM(other.current_price), 0)
                      FROM auctions other
                      WHERE other.status = ?
                        AND other.highest_bidder_id = ?
                        AND other.id <> a.id
                    )
                  ) >= (
                    CASE
                      WHEN a.highest_bidder_id = ? THEN (? - a.current_price)
                      ELSE ?
                    END
                  )
             """;
        // USER có thể đấu giá nhiều bid cùng lúc nên phải lấy balance - tổng các auc khác
        // UPDATE WHERE => ATOMIC UPDATE
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bidderId);
            ps.setDouble(2, amount);
            ps.setString(3, bidderId);
            Timestamp now = Timestamp.valueOf(java.time.LocalDateTime.now());
            ps.setTimestamp(4, now);
            ps.setTimestamp(5, now);
            ps.setString(6, id);
            ps.setString(7, AuctionStatus.RUNNING.name());
            ps.setTimestamp(8, now);
            ps.setTimestamp(9, now);
            ps.setDouble(10, amount);
            ps.setString(11, AuctionStatus.RUNNING.name());
            ps.setString(12, bidderId);
            ps.setString(13, bidderId);
            ps.setDouble(14, amount);
            ps.setDouble(15, amount);
            return ps.executeUpdate() > 0;
        }
    }

    public int markOpenAuctionsAsRunning() throws SQLException {
        String sql = """
                UPDATE auctions
                SET status = ?
                WHERE status = ?
                  AND start_time <= ?
                  AND end_time > ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            Timestamp now = Timestamp.valueOf(java.time.LocalDateTime.now());
            ps.setString(1, AuctionStatus.RUNNING.name());
            ps.setString(2, AuctionStatus.OPEN.name());
            ps.setTimestamp(3, now);
            ps.setTimestamp(4, now);
            return ps.executeUpdate();
        }
    }

    public int markRunningAuctionsAsFinished() throws SQLException {
        String deductSql = """
                UPDATE users u
                JOIN (
                    SELECT highest_bidder_id, SUM(current_price) AS total_amount
                    FROM auctions
                    WHERE status = ?
                      AND end_time <= ?
                      AND highest_bidder_id IS NOT NULL
                    GROUP BY highest_bidder_id
                ) win ON u.id = win.highest_bidder_id
                SET u.balance = u.balance - win.total_amount
                """;

        String finishSql = """
                UPDATE auctions
                SET status = ?
                WHERE status = ?
                  AND end_time <= ?
                """;

        String creditSellerSql = """
                UPDATE users u
                JOIN (
                    SELECT seller_id, SUM(current_price) AS total_amount
                    FROM auctions
                    WHERE status = ?
                      AND end_time <= ?
                      AND highest_bidder_id IS NOT NULL
                    GROUP BY seller_id
                ) sold ON u.id = sold.seller_id
                SET u.balance = u.balance + sold.total_amount
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement deductPs = conn.prepareStatement(deductSql);
             PreparedStatement creditSellerPs = conn.prepareStatement(creditSellerSql);
             PreparedStatement finishPs = conn.prepareStatement(finishSql)) {
            conn.setAutoCommit(false);
            try {
                Timestamp now = Timestamp.valueOf(java.time.LocalDateTime.now());

                // Bước 1: Trừ tiền bidder trước (dùng status RUNNING vì chưa đổi)
                deductPs.setString(1, AuctionStatus.RUNNING.name());
                deductPs.setTimestamp(2, now);
                deductPs.executeUpdate();

                // Bước 2: Cộng tiền seller trước (dùng status RUNNING vì chưa đổi)
                creditSellerPs.setString(1, AuctionStatus.RUNNING.name());
                creditSellerPs.setTimestamp(2, now);
                creditSellerPs.executeUpdate();

                // Bước 3: Đổi status sang FINISHED sau cùng
                finishPs.setString(1, AuctionStatus.FINISHED.name());
                finishPs.setString(2, AuctionStatus.RUNNING.name());
                finishPs.setTimestamp(3, now);
                int finishedRows = finishPs.executeUpdate();

                conn.commit();
                return finishedRows;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // HELPER
    private Auction extractAuction(ResultSet rs) throws SQLException {
        String itemId = rs.getString("item_id");
        String sellerId = rs.getString("seller_id");
        String highestBidderId = rs.getString("highest_bidder_id");

        Item item = itemDAO.findById(itemId);
        User sellerUser = userDAO.findById(sellerId);
        Seller seller = (sellerUser instanceof Seller) ? (Seller) sellerUser : null;
        
        Bidder highestBidder = null;
        if (highestBidderId != null) {
            User bidderUser = userDAO.findById(highestBidderId);
            highestBidder = (bidderUser instanceof Bidder) ? (Bidder) bidderUser : null;
        }

        if (item == null || seller == null) {
            return null; // Corrupted data
        }

        return MapAuctionDB.mapAuction(rs, item, seller, highestBidder);
    }
}
