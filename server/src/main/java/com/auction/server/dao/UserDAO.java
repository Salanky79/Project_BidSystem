package com.auction.server.dao;

import com.auction.server.util.DatabaseConnection;
import com.auction.share.models.user.*;

import javax.xml.transform.Result;
import java.sql.*;

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


    private static User mapUser(ResultSet rs) throws SQLException {
        String uuid = rs.getString("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String fullname = rs.getString("fullname");
        String role = rs.getString("role");
        double balance = rs.getDouble("balance");
        String address = rs.getString("address");
        int accessLevel = rs.getInt("access_level");

        switch (role) {
            case "SELLER":
                Seller seller = new Seller(username, password, fullname);
                seller.setID(uuid);
                seller.setBalance(balance);
                return seller;

            case "BIDDER":
                Bidder bidder = new Bidder(username, password, fullname, address);
                bidder.setID(uuid);
                bidder.setBalance(balance);
                return bidder;

            case "ADMIN":
                Admin admin = new Admin(username, password, fullname, accessLevel);
                admin.setID(uuid);
                return admin;
        }
        return null;
    }



    public static User findById(String id) throws SQLException{
        String sql = "SELECT * FROM users WHERE id = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
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
                    return mapUser(rs);
                    }
                }
            }
        return null;
    }
}
