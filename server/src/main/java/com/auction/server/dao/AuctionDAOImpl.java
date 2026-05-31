package com.auction.server.dao;

import com.auction.server.mapper.AuctionMapper;
import com.auction.share.enums.AuctionStatus;
import com.auction.share.models.auction.Auction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Set;

public class AuctionDAOImpl implements AuctionDAO {
    private static final int SNIPE_THRESHOLD_SECONDS = 10;
    private static final int SNIPE_EXTENSION_SECONDS = 30;


    private List<Auction> queryAuctions(Connection conn, String whereAndOrderClause, Object... params) throws SQLException {
        List<Auction> list = new ArrayList<>();
        String sql = """
            SELECT a.id, a.item_id, a.seller_id, a.current_price, a.highest_bidder_id, a.start_time, a.end_time, a.bid_step, a.status, a.bid_count,
                   i.name AS item_name, i.description AS item_description, i.starting_price AS item_starting_price, i.category AS item_category, i.image_url AS item_image_url,
                   s.username AS seller_username, s.fullname AS seller_fullname, s.phoneNumber AS seller_phoneNumber, s.email AS seller_email, s.address AS seller_address, s.balance AS seller_balance,
                   b.username AS bidder_username, b.fullname AS bidder_fullname, b.phoneNumber AS bidder_phoneNumber, b.email AS bidder_email, b.address AS bidder_address, b.balance AS bidder_balance
            FROM auctions a
            JOIN items i ON a.item_id = i.id
            JOIN users s ON a.seller_id = s.id
            LEFT JOIN users b ON a.highest_bidder_id = b.id
            """ + (whereAndOrderClause != null ? whereAndOrderClause : "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Auction auction = AuctionMapper.extractAuctionFromDB(rs);
                    if (auction != null) {
                        list.add(auction);
                    }
                }
            }
        }
        return list;
    }

    public AuctionDAOImpl() {}

    public boolean saveAuction(Connection conn, Auction auction) throws SQLException {
        String sql = "INSERT INTO auctions (id, item_id, seller_id, current_price, highest_bidder_id, start_time, end_time, bid_step, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
             
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

    public boolean updateStatus(Connection conn, String id, AuctionStatus status) throws SQLException {
        String sql = "UPDATE auctions SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, status.name());
            ps.setString(2, id);
            
            return ps.executeUpdate() > 0;
        }
    }

    public Auction findById(Connection conn, String id) throws SQLException {
        List<Auction> list = queryAuctions(conn, "WHERE a.id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }


    public List<Auction> findAll(Connection conn) throws SQLException {
        return queryAuctions(conn, "ORDER BY a.start_time DESC");
    }

    public List<Auction> findByStatus(Connection conn, AuctionStatus status) throws SQLException {
        return queryAuctions(conn, "WHERE a.status = ? ORDER BY a.start_time DESC", status.name());
    }

    /** Lấy tất cả auction của một seller cụ thể */
    public List<Auction> findBySeller(Connection conn, String sellerId) throws SQLException {
        return queryAuctions(conn, "WHERE a.seller_id = ? ORDER BY a.start_time DESC", sellerId);
    }

    /** Lấy auction của seller cụ thể lọc theo status */
    public List<Auction> findBySellerAndStatus(Connection conn, String sellerId, AuctionStatus status) throws SQLException {
        return queryAuctions(conn, "WHERE a.seller_id = ? AND a.status = ? ORDER BY a.start_time DESC", sellerId, status.name());
    }


    public double sumAuctionCurrentPrices(Connection conn, String bidderId, String excludedAuctionId) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT COALESCE(SUM(current_price), 0) AS total_amount
                FROM auctions
                WHERE status = ?
                  AND highest_bidder_id = ?
                """);

        boolean hasExcludedAuction =
                excludedAuctionId != null && !excludedAuctionId.isEmpty();

        if (hasExcludedAuction) {
            sql.append(" AND id <> ?");
        }

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;

            ps.setString(index++, AuctionStatus.RUNNING.name());
            ps.setString(index++, bidderId);

            if (hasExcludedAuction) {
                ps.setString(index, excludedAuctionId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("total_amount") : 0.0;
            }
        }
    }



    public boolean updateHighestBid(Connection conn, String id, String bidderId, double amount) throws SQLException {
        String sql = """
                UPDATE auctions a
                SET a.current_price = ?,
                    a.highest_bidder_id = ?,
                    a.bid_count = a.bid_count + 1,
                    a.end_time = CASE
                        WHEN TIMESTAMPDIFF(SECOND, CURRENT_TIMESTAMP, a.end_time) <= %d
                             AND TIMESTAMPDIFF(SECOND, CURRENT_TIMESTAMP, a.end_time) >= 0
                             AND (a.highest_bidder_id IS NULL OR a.highest_bidder_id <> ?)
                        THEN DATE_ADD(a.end_time, INTERVAL %d SECOND)
                        ELSE a.end_time
                    END
                WHERE a.id = ?
                  AND a.status = ?
                  AND a.start_time <= CURRENT_TIMESTAMP
                  AND a.end_time > CURRENT_TIMESTAMP
                  AND ? >= (a.current_price + a.bid_step)
             """.formatted(SNIPE_THRESHOLD_SECONDS, SNIPE_EXTENSION_SECONDS);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, bidderId);
            ps.setString(3, bidderId);
            ps.setString(4, id);
            ps.setString(5, AuctionStatus.RUNNING.name());
            ps.setDouble(6, amount);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateBidStep(Connection conn, String auctionId, double step) throws SQLException {
        String sql = "UPDATE auctions SET bid_step = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, step);
            ps.setString(2, auctionId);
            return ps.executeUpdate() > 0;
        }
    }

    public int markOpenAuctionsAsRunning(Connection conn) throws SQLException {
        String sql = """
                UPDATE auctions
                SET status = ?
                WHERE status = ?
                  AND start_time <= CURRENT_TIMESTAMP
                  AND end_time > CURRENT_TIMESTAMP
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, AuctionStatus.RUNNING.name());
            ps.setString(2, AuctionStatus.OPEN.name());
            return ps.executeUpdate();
        }
    }

    public int finishAuctions(Connection conn, List<String> auctionIds) throws SQLException {
        if (auctionIds == null || auctionIds.isEmpty()) return 0;
        String placeholders = String.join(",", java.util.Collections.nCopies(auctionIds.size(), "?"));
        String finishSql = """
                UPDATE auctions
                SET status = ?
                WHERE id IN (%s)
                  AND status = ?
                """.formatted(placeholders);
        try (PreparedStatement finishPs = conn.prepareStatement(finishSql)) {
            finishPs.setString(1, AuctionStatus.FINISHED.name());
            for (int i = 0; i < auctionIds.size(); i++) {
                finishPs.setString(i + 2, auctionIds.get(i));
            }
            finishPs.setString(auctionIds.size() + 2, AuctionStatus.RUNNING.name());
            return finishPs.executeUpdate();
        }
    }

    /** Lấy danh sách auction RUNNING đã hết giờ (end_time <= now). */
    public List<String> findEndedRunningAuctionIds(Connection conn, LocalDateTime now) throws SQLException {
        if (now == null) {
            now = LocalDateTime.now();
        }

        String sql = """
                SELECT id
                FROM auctions
                WHERE status = ?
                  AND end_time <= ?
                """;
        List<String> ids = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, AuctionStatus.RUNNING.name());
            ps.setTimestamp(2, Timestamp.valueOf(now));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    if (id != null && !id.isBlank()) {
                        ids.add(id);
                    }
                }
            }
        }
        return ids;
    }

}
