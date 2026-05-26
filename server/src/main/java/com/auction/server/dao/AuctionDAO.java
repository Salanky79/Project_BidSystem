package com.auction.server.dao;

import com.auction.server.util.DatabaseConnection;
import com.auction.server.util.MapAuctionDB;
import com.auction.share.enums.AuctionStatus;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Set;

public class AuctionDAO {
    private final ItemDAO itemDAO = new ItemDAO();
    private final UserDAO userDAO = new UserDAO();

    public boolean saveAuction(Auction auction) throws SQLException {
        String sql = "INSERT INTO auctions (id, item_id, seller_id, current_price, highest_bidder_id, start_time, end_time, bid_step, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            ps.setDouble(8, auction.getBidStep());
            ps.setString(9, auction.getStatus().name());
            
            int row = ps.executeUpdate();
            return row > 0;
        }
    }

    public boolean updateStatus(String id, AuctionStatus status) throws SQLException {
        String sql = "UPDATE auctions SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, status.name());
            ps.setString(2, id);
            
            return ps.executeUpdate() > 0;
        }
    }

    public Auction findById(Connection conn, String id) throws SQLException {
        String sql = """
                SELECT a.id, a.item_id, a.seller_id, a.current_price, a.highest_bidder_id, a.start_time, a.end_time, a.bid_step, a.status,
                       i.name AS item_name, i.description AS item_description, i.starting_price AS item_starting_price, i.category AS item_category, i.image_url AS item_image_url,
                       s.username AS seller_username, s.fullname AS seller_fullname, s.phoneNumber AS seller_phoneNumber, s.email AS seller_email, s.address AS seller_address, s.balance AS seller_balance,
                       b.username AS bidder_username, b.fullname AS bidder_fullname, b.phoneNumber AS bidder_phoneNumber, b.email AS bidder_email, b.address AS bidder_address, b.balance AS bidder_balance
                FROM auctions a
                JOIN items i ON a.item_id = i.id
                JOIN users s ON a.seller_id = s.id
                LEFT JOIN users b ON a.highest_bidder_id = b.id
                WHERE a.id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractAuctionJoined(rs);
                }
            }
        }
        return null;
    }

    public Auction findById(String id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return findById(conn, id);
        }
    }

    public List<Auction> findAll() throws SQLException {
        List<Auction> list = new ArrayList<>();
        String sql = """
                SELECT a.id, a.item_id, a.seller_id, a.current_price, a.highest_bidder_id, a.start_time, a.end_time, a.bid_step, a.status,
                       i.name AS item_name, i.description AS item_description, i.starting_price AS item_starting_price, i.category AS item_category, i.image_url AS item_image_url,
                       s.username AS seller_username, s.fullname AS seller_fullname, s.phoneNumber AS seller_phoneNumber, s.email AS seller_email, s.address AS seller_address, s.balance AS seller_balance,
                       b.username AS bidder_username, b.fullname AS bidder_fullname, b.phoneNumber AS bidder_phoneNumber, b.email AS bidder_email, b.address AS bidder_address, b.balance AS bidder_balance
                FROM auctions a
                JOIN items i ON a.item_id = i.id
                JOIN users s ON a.seller_id = s.id
                LEFT JOIN users b ON a.highest_bidder_id = b.id
                ORDER BY a.start_time DESC
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                Auction auction = extractAuctionJoined(rs);
                if (auction != null) {
                    list.add(auction);
                }
            }
        }
        return list;
    }

    public List<Auction> findByStatus(AuctionStatus status) throws SQLException {
        List<Auction> list = new ArrayList<>();
        String sql = """
                SELECT a.id, a.item_id, a.seller_id, a.current_price, a.highest_bidder_id, a.start_time, a.end_time, a.bid_step, a.status,
                       i.name AS item_name, i.description AS item_description, i.starting_price AS item_starting_price, i.category AS item_category, i.image_url AS item_image_url,
                       s.username AS seller_username, s.fullname AS seller_fullname, s.phoneNumber AS seller_phoneNumber, s.email AS seller_email, s.address AS seller_address, s.balance AS seller_balance,
                       b.username AS bidder_username, b.fullname AS bidder_fullname, b.phoneNumber AS bidder_phoneNumber, b.email AS bidder_email, b.address AS bidder_address, b.balance AS bidder_balance
                FROM auctions a
                JOIN items i ON a.item_id = i.id
                JOIN users s ON a.seller_id = s.id
                LEFT JOIN users b ON a.highest_bidder_id = b.id
                WHERE a.status = ?
                ORDER BY a.start_time DESC
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Auction auction = extractAuctionJoined(rs);
                    if (auction != null) {
                        list.add(auction);
                    }
                }
            }
        }
        return list;
    }

    /** Lấy tất cả auction của một seller cụ thể */
    public List<Auction> findBySeller(String sellerId) throws SQLException {
        List<Auction> list = new ArrayList<>();
        String sql = """
                SELECT a.id, a.item_id, a.seller_id, a.current_price, a.highest_bidder_id, a.start_time, a.end_time, a.bid_step, a.status,
                       i.name AS item_name, i.description AS item_description, i.starting_price AS item_starting_price, i.category AS item_category, i.image_url AS item_image_url,
                       s.username AS seller_username, s.fullname AS seller_fullname, s.phoneNumber AS seller_phoneNumber, s.email AS seller_email, s.address AS seller_address, s.balance AS seller_balance,
                       b.username AS bidder_username, b.fullname AS bidder_fullname, b.phoneNumber AS bidder_phoneNumber, b.email AS bidder_email, b.address AS bidder_address, b.balance AS bidder_balance
                FROM auctions a
                JOIN items i ON a.item_id = i.id
                JOIN users s ON a.seller_id = s.id
                LEFT JOIN users b ON a.highest_bidder_id = b.id
                WHERE a.seller_id = ?
                ORDER BY a.start_time DESC
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Auction auction = extractAuctionJoined(rs);
                    if (auction != null) {
                        list.add(auction);
                    }
                }
            }
        }
        return list;
    }

    /** Lấy auction của seller cụ thể lọc theo status */
    public List<Auction> findBySellerAndStatus(String sellerId, AuctionStatus status) throws SQLException {
        List<Auction> list = new ArrayList<>();
        String sql = """
                SELECT a.id, a.item_id, a.seller_id, a.current_price, a.highest_bidder_id, a.start_time, a.end_time, a.bid_step, a.status,
                       i.name AS item_name, i.description AS item_description, i.starting_price AS item_starting_price, i.category AS item_category, i.image_url AS item_image_url,
                       s.username AS seller_username, s.fullname AS seller_fullname, s.phoneNumber AS seller_phoneNumber, s.email AS seller_email, s.address AS seller_address, s.balance AS seller_balance,
                       b.username AS bidder_username, b.fullname AS bidder_fullname, b.phoneNumber AS bidder_phoneNumber, b.email AS bidder_email, b.address AS bidder_address, b.balance AS bidder_balance
                FROM auctions a
                JOIN items i ON a.item_id = i.id
                JOIN users s ON a.seller_id = s.id
                LEFT JOIN users b ON a.highest_bidder_id = b.id
                WHERE a.seller_id = ? AND a.status = ?
                ORDER BY a.start_time DESC
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sellerId);
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Auction auction = extractAuctionJoined(rs);
                    if (auction != null) {
                        list.add(auction);
                    }
                }
            }
        }
        return list;
    }


    public double sumRunningWinningBidsByBidder(Connection conn, String bidderId, Set<String> excludedAuctionIds) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT COALESCE(SUM(current_price), 0) AS total_amount
                FROM auctions
                WHERE status = ?
                  AND highest_bidder_id = ?
                """);

        if (excludedAuctionIds != null && !excludedAuctionIds.isEmpty()) {
            sql.append(" AND id NOT IN (");
            sql.append("?,".repeat(excludedAuctionIds.size()));
            sql.setLength(sql.length() - 1);
            sql.append(")");
        }

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            ps.setString(index++, AuctionStatus.RUNNING.name());
            ps.setString(index++, bidderId);
            if (excludedAuctionIds != null) {
                for (String auctionId : excludedAuctionIds) {
                    ps.setString(index++, auctionId);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("total_amount") : 0.0;
            }
        }
    }

    public double sumRunningWinningBidsByBidder(String bidderId, Set<String> excludedAuctionIds) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return sumRunningWinningBidsByBidder(conn, bidderId, excludedAuctionIds);
        }
    }

    private double findUserBalance(Connection conn, String userId) throws SQLException {
        String sql = "SELECT balance FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        }
        return 0.0;
    }


    public boolean updateHighestBidIfHigher(Connection conn, String id, String bidderId, double amount) throws SQLException {
        Auction auction = findById(conn, id);
        if (auction == null) {
            return false;
        }

        double reservedInOtherRunningAuctions = sumRunningWinningBidsByBidder(conn, bidderId, Set.of(id));
        double requiredForThisBid = amount;
        if (auction.getHighestBidder() != null && bidderId.equals(auction.getHighestBidder().getId())) {
            requiredForThisBid = amount - auction.getCurrentHighestBid();
        }
        double bidderBalance = findUserBalance(conn, bidderId);
        if (bidderBalance - reservedInOtherRunningAuctions < requiredForThisBid) {
            return false;
        }

        String sql = """
                UPDATE auctions a
                SET a.current_price = ?,
                    a.highest_bidder_id = ?,
                    a.end_time = CASE
                        WHEN TIMESTAMPDIFF(SECOND, ?, a.end_time) <= 10
                             AND TIMESTAMPDIFF(SECOND, ?, a.end_time) >= 0
                             AND (a.highest_bidder_id IS NULL OR a.highest_bidder_id <> ?)
                        THEN DATE_ADD(a.end_time, INTERVAL 30 SECOND)
                        ELSE a.end_time
                    END
                WHERE a.id = ?
                  AND a.status = ?
                  AND a.start_time <= ?
                  AND a.end_time > ?
                  AND ? >= (a.current_price + a.bid_step)
             """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, bidderId);
            Timestamp now = Timestamp.valueOf(java.time.LocalDateTime.now());
            ps.setTimestamp(3, now);
            ps.setTimestamp(4, now);
            ps.setString(5, bidderId);
            ps.setString(6, id);
            ps.setString(7, AuctionStatus.RUNNING.name());
            ps.setTimestamp(8, now);
            ps.setTimestamp(9, now);
            ps.setDouble(10, amount);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateBidStep(String auctionId, double step) throws SQLException {
        String sql = "UPDATE auctions SET bid_step = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, step);
            ps.setString(2, auctionId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateEndTime(String auctionId, LocalDateTime newEndTime) throws SQLException {
        String sql = "UPDATE auctions SET end_time = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(newEndTime));
            ps.setString(2, auctionId);
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

        try (Connection conn = DatabaseConnection.getConnection()) {
            Timestamp now = Timestamp.valueOf(java.time.LocalDateTime.now());

            // Check if there are any expired running auctions first to avoid locking
            String checkSql = "SELECT COUNT(*) FROM auctions WHERE status = ? AND end_time <= ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, AuctionStatus.RUNNING.name());
                checkPs.setTimestamp(2, now);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        return 0; // No expired auctions, skip heavy updates!
                    }
                }
            }

            try (PreparedStatement deductPs = conn.prepareStatement(deductSql);
                 PreparedStatement creditSellerPs = conn.prepareStatement(creditSellerSql);
                 PreparedStatement finishPs = conn.prepareStatement(finishSql)) {
                conn.setAutoCommit(false);
                try {
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
    }

    // HELPER
    private Auction extractAuctionJoined(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String sellerId = rs.getString("seller_id");
        
        // Map Item
        String itemId = rs.getString("item_id");
        String itemName = rs.getString("item_name");
        String itemDescription = rs.getString("item_description");
        double itemStartingPrice = rs.getDouble("item_starting_price");
        String itemCategory = rs.getString("item_category");
        com.auction.share.enums.Category category;
        try {
            category = com.auction.share.enums.Category.valueOf(itemCategory.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            category = com.auction.share.enums.Category.ITEM;
        }
        Item item = new Item(itemName, itemDescription, itemStartingPrice, sellerId, category);
        item.setID(itemId);
        item.setImageUrl(rs.getString("item_image_url"));

        // Map Seller
        String sellerUsername = rs.getString("seller_username");
        String sellerFullName = rs.getString("seller_fullname");
        String sellerPhone = rs.getString("seller_phoneNumber");
        String sellerEmail = rs.getString("seller_email");
        String sellerAddress = rs.getString("seller_address");
        double sellerBalance = rs.getDouble("seller_balance");
        Seller seller = new Seller(sellerUsername, null, sellerFullName, sellerPhone, sellerEmail, sellerAddress);
        seller.setID(sellerId);
        seller.setBalance(sellerBalance);

        // Map Highest Bidder
        String bidderId = rs.getString("highest_bidder_id");
        Bidder highestBidder = null;
        if (bidderId != null) {
            String bidderUsername = rs.getString("bidder_username");
            String bidderFullName = rs.getString("bidder_fullname");
            String bidderPhone = rs.getString("bidder_phoneNumber");
            String bidderEmail = rs.getString("bidder_email");
            String bidderAddress = rs.getString("bidder_address");
            double bidderBalance = rs.getDouble("bidder_balance");
            highestBidder = new Bidder(bidderUsername, null, bidderFullName, bidderPhone, bidderEmail, bidderAddress);
            highestBidder.setID(bidderId);
            highestBidder.setBalance(bidderBalance);
        }

        Timestamp startTimestamp = rs.getTimestamp("start_time");
        Timestamp endTimestamp = rs.getTimestamp("end_time");
        LocalDateTime startTime = startTimestamp != null ? startTimestamp.toLocalDateTime() : null;
        LocalDateTime endTime = endTimestamp != null ? endTimestamp.toLocalDateTime() : null;

        Auction auction = new Auction(item, seller, startTime, endTime);
        auction.setID(id);

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                switch (AuctionStatus.valueOf(statusStr)) {
                    case RUNNING:
                        auction.markRunning();
                        break;
                    case FINISHED:
                        auction.markFinished();
                        break;
                    case CANCELED:
                        auction.markCanceled();
                        break;
                    case OPEN:
                    default:
                        break;
                }
            } catch (IllegalArgumentException ignored) {}
        }

        double currentPrice = rs.getDouble("current_price");
        double bidStep = rs.getDouble("bid_step");
        auction.setBidStep(bidStep);
        if (highestBidder != null) {
            auction.setHighestBid(highestBidder, currentPrice);
        }

        return auction;
    }
}
