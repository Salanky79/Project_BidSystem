package com.auction.server.dao;

import com.auction.server.mapper.UserMapper;
import com.auction.share.models.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.nCopies;

public class UserDAO  {
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
        List<Object> params = new ArrayList<>();

        if (fullName != null && !fullName.isBlank()) { sql.append("fullname = ?, "); params.add(fullName); }
        if (email != null && !email.isBlank()) { sql.append("email = ?, "); params.add(email); }
        if (address != null && !address.isBlank()) { sql.append("address = ?, "); params.add(address); }
        if (phoneNumber != null && !phoneNumber.isBlank()) { sql.append("phoneNumber = ?, "); params.add(phoneNumber); }
        if (password != null && !password.isBlank()) { sql.append("password = ?, "); params.add(password); }

        if (params.isEmpty()) return false; // nothing to update
        
        sql.setLength(sql.length() - 2); // remove last ", "
        sql.append(" WHERE id = ?");
        params.add(userId);

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
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

    public int deductWinningBidders(Connection conn, List<String> auctionIds) throws SQLException {
        if (auctionIds == null || auctionIds.isEmpty()) return 0;
        String placeholders = String.join(",", nCopies(auctionIds.size(), "?"));
        String sql = """
                UPDATE users u
                JOIN (
                    SELECT highest_bidder_id, SUM(current_price) AS total_amount
                    FROM auctions
                    WHERE id IN (%s)
                      AND highest_bidder_id IS NOT NULL
                    GROUP BY highest_bidder_id
                ) win ON u.id = win.highest_bidder_id
                SET u.balance = u.balance - win.total_amount
                """.formatted(placeholders);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < auctionIds.size(); i++) {
                ps.setString(i + 1, auctionIds.get(i));
            }
            return ps.executeUpdate();
        }
    }

    public int creditSellers(Connection conn, List<String> auctionIds) throws SQLException {
        if (auctionIds == null || auctionIds.isEmpty()) return 0;
        String placeholders = String.join(",", nCopies(auctionIds.size(), "?"));
        String sql = """
                UPDATE users u
                JOIN (
                    SELECT seller_id, SUM(current_price) AS total_amount
                    FROM auctions
                    WHERE id IN (%s)
                      AND highest_bidder_id IS NOT NULL
                    GROUP BY seller_id
                ) sold ON u.id = sold.seller_id
                SET u.balance = u.balance + sold.total_amount
                """.formatted(placeholders);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < auctionIds.size(); i++) {
                ps.setString(i + 1, auctionIds.get(i));
            }
            return ps.executeUpdate();
        }
    }

    public boolean updateBalance(Connection conn, String userId, double amount) throws SQLException {
        String sql = "UPDATE users SET balance = balance + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        }
    }
}

