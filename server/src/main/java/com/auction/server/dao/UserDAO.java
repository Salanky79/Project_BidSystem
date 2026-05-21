package com.auction.server.dao;

import com.auction.server.util.MapUserDB;
import com.auction.server.util.DatabaseConnection;
import com.auction.share.models.user.Admin;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO truy cập dữ liệu người dùng.
 */
public class UserDAO {
    // Lưu tài khoản mới.
    public boolean saveUser(User user) throws SQLException {
        String sql = "INSERT INTO users (id, fullname, username, password, phoneNumber, email, role, balance, address, access_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getId());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getPassword());
            ps.setString(7, user.getRole().name());
            bindRoleSpecificFields(ps, user);

            int row = ps.executeUpdate();
            return row > 0;
        }
    }

    // Kiểm tra username đã tồn tại.
    public boolean isUsernameTaken(String username) throws SQLException{
        String sql = "SELECT username FROM users WHERE username = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, username);

            try(ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public User findById(String id) throws SQLException{
        String sql = "SELECT * FROM users WHERE id = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return MapUserDB.mapUser(rs);
                }
            }
        }
        return null;
    }

    public User findByUsername(String username) throws SQLException{
        String sql = "SELECT * FROM users WHERE username = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, username);

            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return MapUserDB.mapUser(rs);
                    }
                }
            }
        return null;
    }

    public boolean updateUserPassword(String id, String password) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, password);
            ps.setString(2, id);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateUserFullName(String id, String fullName) throws SQLException {
        String sql = "UPDATE users SET fullname = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, id);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateUserAddress(String id, String address) throws SQLException {
        String sql = "UPDATE users SET address = ? WHERE id = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, address);
            ps.setString(2, id);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateUserEmail(String id, String email) throws SQLException {
        String sql = "UPDATE users SET email = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, id);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateUserPhoneNumber(String id, String phoneNumber) throws SQLException{
        String sql = "UPDATE users SET phoneNumber = ? WHERE id = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phoneNumber);
            ps.setString(2, id);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean isEmailTaken(String email) throws SQLException {
        String sql = "SELECT email FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean updateBalance(String id, double newBalance) throws SQLException {
        String sql = "UPDATE users SET balance = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Helper.
    private void bindRoleSpecificFields(PreparedStatement ps, User user) throws SQLException {
        if (user instanceof Bidder bidder) {
            ps.setString(5, bidder.getPhoneNumber());
            ps.setString(6, bidder.getEmail());
            ps.setDouble(8, bidder.getBalance());
            ps.setString(9, bidder.getAddress());
            ps.setInt(10, 0);
            return;
        }

        if (user instanceof Seller seller) {
            ps.setString(5, seller.getPhoneNumber());
            ps.setString(6, seller.getEmail());
            ps.setDouble(8, seller.getBalance());
            ps.setString(9, seller.getAddress());
            ps.setInt(10, 0);
            return;
        }

        if (user instanceof Admin admin) {
            ps.setNull(5, java.sql.Types.VARCHAR);
            ps.setNull(6, java.sql.Types.VARCHAR);
            ps.setDouble(8, 0.0);
            ps.setNull(9, java.sql.Types.VARCHAR);
            ps.setInt(10, admin.getAccessLevel());
            return;
        }

        throw new IllegalArgumentException("Unsupported user type: " + user.getClass().getSimpleName());
    }
}