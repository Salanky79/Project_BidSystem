package com.auction.server.util;

import com.auction.share.models.user.Admin;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class MapUserDB {
    private MapUserDB() {
    }

    public static User mapUser(ResultSet rs) throws SQLException {
        String uuid = rs.getString("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String fullName = rs.getString("fullname");
        String phoneNumber = rs.getString("phoneNumber");
        String email = rs.getString("email");
        String role = rs.getString("role");
        double balance = rs.getDouble("balance");
        String address = rs.getString("address");
        int accessLevel = rs.getInt("access_level");

        User user;
        switch (role) {
            case "SELLER":
                Seller seller = new Seller(username, password, fullName, phoneNumber, email, address);
                seller.setBalance(balance);
                user = seller;
                break;
            case "BIDDER":
                Bidder bidder = new Bidder(username, password, fullName, phoneNumber, email, address);
                bidder.setBalance(balance);
                user = bidder;
                break;
            case "ADMIN":
                user = new Admin(username, password, fullName, accessLevel);
                break;
            default:
                throw new SQLException("Unsupported role from database: " + role);
        }

        user.setID(uuid);
        return user;
    }
}
