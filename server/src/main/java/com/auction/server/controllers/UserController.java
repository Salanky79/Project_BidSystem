package com.auction.server.controllers;

import com.auction.server.util.PasswordUtil;
import com.auction.share.models.user.User;
import com.auction.server.dao.UserDAO;

import java.sql.SQLException;

public class UserController {

    public static boolean register(User user) throws SQLException {
        String hashPass = PasswordUtil.hashPassword(user.getPassword());






    }
}
