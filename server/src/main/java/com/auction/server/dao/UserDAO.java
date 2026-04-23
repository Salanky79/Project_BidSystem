package com.auction.server.dao;

import com.auction.server.factory.UserDBFactory;
import com.auction.server.util.DatabaseConnection;
import com.auction.share.models.user.*;


import java.sql.*;
import com.auction.server.factory.UserDBFactory;

public class UserDAO {
    // LƯU TÀI KHOẢN MỚI
    public static boolean saveUser(User user) throws SQLException {
        String sql = "INSERT INTO users (id, fullname, username, password, role, balance, address, access_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getId());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getRole().name());
            ps.setDouble(6, user.getBalance());
            ps.setString(7, user.getAddress());
            ps.setInt(8, user.getAccessLevel());

            int row = ps.executeUpdate();
            return row > 0;
        }
    }

    // KIỂM TRA USERNAME ĐÃ TỒN TẠI CHƯA
    public static boolean isUsernameTaken(String username) throws SQLException{
        String sql = "SELECT username FROM users WHERE username = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, username);

            try(ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


    public static User findById(String id) throws SQLException{
        String sql = "SELECT * FROM users WHERE id = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return UserDBFactory.mapUser(rs);
                }
            }
        }
        return null;
    }


    public static User findByUsername(String username) throws SQLException{
        String sql = "SELECT * FROM users WHERE username = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, username);

            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return UserDBFactory.mapUser(rs);
                    }
                }
            }
        return null;
    }

    public static boolean updateUserPassword(String id, String password) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, password);
            ps.setString(2, id);

            return ps.executeUpdate() > 0;
        }
    }

    public static boolean updateUserAddress(String id, String address) throws SQLException {
        String sql = "UPDATE users SET address = ? WHERE id = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, address);
            ps.setString(2, id);

            return ps.executeUpdate() > 0;
        }
    }
}
