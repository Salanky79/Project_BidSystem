package com.auction.server;

import com.auction.server.dao.UserDAO;
import com.auction.server.util.PasswordUtil;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.User;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        Bidder bidder2 = new Bidder(
                "bidder2",
                PasswordUtil.hashPassword("123456"),
                "Le Van C",
                "0911222333",
                "bidder2@gmail.com",
                "HCM"
        );
        UserDAO userDAO = new UserDAO();
        userDAO.saveUser(bidder2);
    }
}
