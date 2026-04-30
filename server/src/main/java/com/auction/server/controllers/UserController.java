package com.auction.server.controllers;

import com.auction.server.exceptions.DataValidationException;
import com.auction.server.util.PasswordUtil;
import com.auction.share.exceptions.AuctionSystemException;
import com.auction.share.models.user.User;
import com.auction.server.dao.UserDAO;

import java.sql.SQLException;

public class UserController {

    public static boolean register(User user) throws SQLException, AuctionSystemException {
        String hashPass = PasswordUtil.hashPassword(user.getPassword());

        if (UserDAO.isUsernameTaken(user.getUsername())) {
            throw new DataValidationException("Username '" + user.getUsername() + "' is already taken.");
        }

        return UserDAO.saveUser(user);
    }
}