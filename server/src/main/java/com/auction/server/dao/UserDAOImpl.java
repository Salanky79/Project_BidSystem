package com.auction.server.dao;

import com.auction.server.mapper.UserMapper;
import com.auction.share.models.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAOImpl implements UserDAO {
    // LƯU TÀI KHOẢN MỚI
    public boolean saveUser(Connection conn, User user) throws SQLException {
        String sql = "INSERT INTO users (id, fullname, username, password, phoneNumber, email, role, balance, address, access_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getId());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getPassword());
            ps.setString(7, user.getRole().name());
            UserMapper.fillSpecificFields(ps, user);

            int row = ps.executeUpdate();
            return row > 0;
        }
    }

    // KIỂM TRA USERNAME ĐÃ TỒN TẠI CHƯA
    public boolean isUsernameTaken(Connection conn, String username) throws SQLException{
        String sql = "SELECT username FROM users WHERE username = ?";
        try(PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, username);

            try(ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


    public User findById(Connection conn, String id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return UserMapper.extractUserFromDB(rs);
                }
            }
        }
        return null;
    }


    public User findByUsername(Connection conn, String username) throws SQLException{
        String sql = "SELECT * FROM users WHERE username = ?";
        try(PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, username);

            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return UserMapper.extractUserFromDB(rs);
                    }
                }
            }
        return null;
    }



    public boolean isEmailTaken(Connection conn, String email) throws SQLException {
        String sql = "SELECT email FROM users WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean updateProfile(Connection conn, String userId, String fullName, String email, String address, String phoneNumber, String password) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE users SET ");
        boolean first = true;
        if (fullName != null && !fullName.isBlank()) { sql.append("fullname = ?"); first = false; }
        if (email != null && !email.isBlank()) { if (!first) sql.append(", "); sql.append("email = ?"); first = false; }
        if (address != null && !address.isBlank()) { if (!first) sql.append(", "); sql.append("address = ?"); first = false; }
        if (phoneNumber != null && !phoneNumber.isBlank()) { if (!first) sql.append(", "); sql.append("phoneNumber = ?"); first = false; }
        if (password != null && !password.isBlank()) { if (!first) sql.append(", "); sql.append("password = ?"); first = false; }

        if (first) return false; // nothing to update
        
        sql.append(" WHERE id = ?");
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            if (fullName != null && !fullName.isBlank()) ps.setString(index++, fullName);
            if (email != null && !email.isBlank()) ps.setString(index++, email);
            if (address != null && !address.isBlank()) ps.setString(index++, address);
            if (phoneNumber != null && !phoneNumber.isBlank()) ps.setString(index++, phoneNumber);
            if (password != null && !password.isBlank()) ps.setString(index++, password);
            ps.setString(index, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public double findBalanceForUpdate(Connection conn, String userId) throws SQLException {
        String sql = "SELECT balance FROM users WHERE id = ? FOR UPDATE";
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

    public int deductWinningBidders(Connection conn, java.sql.Timestamp endTime) throws SQLException {
        String sql = """
                UPDATE users u
                JOIN (
                    SELECT highest_bidder_id, SUM(current_price) AS total_amount
                    FROM auctions
                    WHERE status = 'RUNNING'
                      AND end_time <= ?
                      AND highest_bidder_id IS NOT NULL
                    GROUP BY highest_bidder_id
                ) win ON u.id = win.highest_bidder_id
                SET u.balance = u.balance - win.total_amount
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, endTime);
            return ps.executeUpdate();
        }
    }

    public int creditSellers(Connection conn, java.sql.Timestamp endTime) throws SQLException {
        String sql = """
                UPDATE users u
                JOIN (
                    SELECT seller_id, SUM(current_price) AS total_amount
                    FROM auctions
                    WHERE status = 'RUNNING'
                      AND end_time <= ?
                      AND highest_bidder_id IS NOT NULL
                    GROUP BY seller_id
                ) sold ON u.id = sold.seller_id
                SET u.balance = u.balance + sold.total_amount
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, endTime);
            return ps.executeUpdate();
        }
    }
}
