package com.auction.server.factory;

import com.auction.share.models.user.Admin;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDBFactory {
    public static User mapUser(ResultSet rs) throws SQLException {
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
}
