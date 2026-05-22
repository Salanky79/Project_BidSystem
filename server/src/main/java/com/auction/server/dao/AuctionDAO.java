package com.auction.server.dao;

import com.auction.server.util.DatabaseConnection;
import com.auction.server.util.MapAuctionDB;
import com.auction.share.enums.AuctionStatus;
import com.auction.share.enums.Category;
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
import java.util.Set;
import java.util.List;
import java.time.LocalDateTime;

public class AuctionDAO {
    private static final String LIST_AUCTIONS_SELECT = """
            SELECT
              a.id AS auction_id,
              a.current_price AS auction_current_price,
              a.highest_bidder_id AS auction_highest_bidder_id,
              a.start_time AS auction_start_time,
              a.end_time AS auction_end_time,
              a.status AS auction_status,
              i.id AS item_id,
              i.seller_id AS item_seller_id,
              i.name AS item_name,
              i.category AS item_category,
              i.starting_price AS item_starting_price,
              i.description AS item_description,
              s.id AS seller_id,
              s.fullname AS seller_fullname,
              s.username AS seller_username,
              s.password AS seller_password,
              s.phoneNumber AS seller_phone,
              s.email AS seller_email,
              s.address AS seller_address,
              s.balance AS seller_balance,
              h.id AS bidder_id,
              h.fullname AS bidder_fullname,
              h.username AS bidder_username,
              h.password AS bidder_password,
              h.phoneNumber AS bidder_phone,
              h.email AS bidder_email,
              h.address AS bidder_address,
              h.balance AS bidder_balance
            FROM auctions a
            JOIN items i ON i.id = a.item_id
            JOIN users s ON s.id = a.seller_id
            LEFT JOIN users h ON h.id = a.highest_bidder_id
            """;

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
        String sql = LIST_AUCTIONS_SELECT + " ORDER BY a.start_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                Auction auction = extractAuctionFromJoin(rs);
                if (auction != null) {
                    list.add(auction);
                }
            }
        }
        return list;
    }

    public List<Auction> findByStatus(AuctionStatus status) throws SQLException {
        List<Auction> list = new ArrayList<>();
        String sql = LIST_AUCTIONS_SELECT + " WHERE a.status = ? ORDER BY a.start_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Auction auction = extractAuctionFromJoin(rs);
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
        String sql = "SELECT * FROM auctions WHERE seller_id = ? ORDER BY start_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sellerId);
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

    /** Lấy auction của seller cụ thể lọc theo status */
    public List<Auction> findBySellerAndStatus(String sellerId, AuctionStatus status) throws SQLException {
        List<Auction> list = new ArrayList<>();
        String sql = "SELECT * FROM auctions WHERE seller_id = ? AND status = ? ORDER BY start_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sellerId);
            ps.setString(2, status.name());
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

    public double sumRunningWinningBidsByBidder(String bidderId, Set<String> excludedAuctionIds) throws SQLException {
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

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
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


    public boolean updateHighestBidIfHigher(Connection conn, String id, String bidderId, double amount) throws SQLException {
        String sql = """
                UPDATE auctions a
                JOIN users u ON u.id = ?
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
                  AND (
                    u.balance - (
                      SELECT COALESCE(SUM(other.current_price), 0)
                      FROM (
                          SELECT current_price
                          FROM auctions
                          WHERE status = ?
                            AND highest_bidder_id = ?
                            AND id <> ?
                      ) other
                    )
                  ) >= (
                    CASE
                      WHEN a.highest_bidder_id = ? THEN (? - a.current_price)
                      ELSE ?
                    END
                  )
             """;
        // USER có thể đấu giá nhiều bid cùng lúc nên phải lấy balance - tổng các auc khác
        // Where đúng => chạy SET
        // check nếu chưa có người bid or xem người bid phải khác nhau mới được bid
        // <> : khác
        // vẫn dùng cas >= 0 vì có thể TIMESTAMPDIFF sẽ làm tròn xuống (logic status đã được check trước ở Where rồi)
        // UPDATE WHERE => ATOMIC UPDATE
        // a.end_time - now (tg còn lại)
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bidderId);
            ps.setDouble(2, amount);
            ps.setString(3, bidderId);
            Timestamp now = Timestamp.valueOf(java.time.LocalDateTime.now());
            ps.setTimestamp(4, now);
            ps.setTimestamp(5, now);
            ps.setString(6, bidderId);
            ps.setString(7, id);
            ps.setString(8, AuctionStatus.RUNNING.name());
            ps.setTimestamp(9, now);
            ps.setTimestamp(10, now);
            ps.setDouble(11, amount);
            ps.setString(12, AuctionStatus.RUNNING.name());
            ps.setString(13, bidderId);
            ps.setString(14, id);
            ps.setString(15, bidderId);
            ps.setDouble(16, amount);
            ps.setDouble(17, amount);
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

    private Auction extractAuctionFromJoin(ResultSet rs) throws SQLException {
        Item item = mapJoinedItem(rs);
        Seller seller = mapJoinedSeller(rs);
        Bidder highestBidder = mapJoinedHighestBidder(rs);
        if (item == null || seller == null) {
            return null;
        }

        Timestamp startTimestamp = rs.getTimestamp("auction_start_time");
        Timestamp endTimestamp = rs.getTimestamp("auction_end_time");
        Auction auction = new Auction(
                item,
                seller,
                startTimestamp != null ? startTimestamp.toLocalDateTime() : null,
                endTimestamp != null ? endTimestamp.toLocalDateTime() : null
        );
        auction.setID(rs.getString("auction_id"));

        String status = rs.getString("auction_status");
        if (status != null) {
            switch (AuctionStatus.valueOf(status)) {
                case RUNNING -> auction.markRunning();
                case FINISHED -> auction.markFinished();
                case CANCELED -> auction.markCanceled();
                case OPEN -> {
                }
            }
        }

        if (highestBidder != null) {
            auction.setHighestBid(highestBidder, rs.getDouble("auction_current_price"));
        }
        return auction;
    }

    private Item mapJoinedItem(ResultSet rs) throws SQLException {
        String categoryRaw = rs.getString("item_category");
        Category category;
        try {
            category = Category.valueOf(categoryRaw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            category = Category.ITEM;
        }

        Item item = new Item(
                rs.getString("item_name"),
                rs.getString("item_description"),
                rs.getDouble("item_starting_price"),
                rs.getString("item_seller_id"),
                category
        );
        item.setID(rs.getString("item_id"));
        return item;
    }

    private Seller mapJoinedSeller(ResultSet rs) throws SQLException {
        Seller seller = new Seller(
                rs.getString("seller_username"),
                rs.getString("seller_password"),
                rs.getString("seller_fullname"),
                rs.getString("seller_phone"),
                rs.getString("seller_email"),
                rs.getString("seller_address")
        );
        seller.setBalance(rs.getDouble("seller_balance"));
        seller.setID(rs.getString("seller_id"));
        return seller;
    }

    private Bidder mapJoinedHighestBidder(ResultSet rs) throws SQLException {
        String bidderId = rs.getString("bidder_id");
        if (bidderId == null) {
            return null;
        }

        Bidder bidder = new Bidder(
                rs.getString("bidder_username"),
                rs.getString("bidder_password"),
                rs.getString("bidder_fullname"),
                rs.getString("bidder_phone"),
                rs.getString("bidder_email"),
                rs.getString("bidder_address")
        );
        bidder.setBalance(rs.getDouble("bidder_balance"));
        bidder.setID(bidderId);
        return bidder;
    }
}
