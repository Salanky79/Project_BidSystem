package com.auction.server.service;

import com.auction.server.dao.UserDAO;
import com.auction.server.util.PasswordUtil;
import com.auction.share.exceptions.AuthenticationException;
import com.auction.share.exceptions.DuplicateResourceException;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;

import java.sql.SQLException;

// Controller → Service → DAO → Database
public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean register(User user) throws SQLException, ValidationException, DuplicateResourceException {
        if (userDAO.isUsernameTaken(user.getUsername())) {
            throw new DuplicateResourceException("Username already exists.");
        }

        String email = extractEmail(user);
        if (email != null && userDAO.isEmailTaken(email)) {
            throw new DuplicateResourceException("Email already exists.");
        }

        user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
        // dung ham bam de ko luu bang string
        return userDAO.saveUser(user);
    }

    public User login(String username, String password) throws SQLException, AuthenticationException, ValidationException {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            throw new AuthenticationException("Account does not exist.");
        }
        if (!PasswordUtil.checkPassword(password, user.getPassword())) {
            throw new AuthenticationException("Incorrect password.");
        }
        return user;
    }

    // tim ID trong DATABASE
    public User getById(String id) throws SQLException, ValidationException {
        User user = userDAO.findById(id);
        if (user == null) {
            throw new ValidationException("User not found.");
        }
        return user;
    }


    // cac ham update thong tin nguoi dung
    public boolean updatePassword(String id, String password) throws SQLException {
        String hashedPassword = PasswordUtil.hashPassword(password);
        return userDAO.updateUserPassword(id, hashedPassword);
    }

    public boolean updateEmail(String id, String email) throws SQLException {
        return userDAO.updateUserEmail(id, email);
    }

    public boolean updateAddress(String id, String address) throws SQLException {
        return userDAO.updateUserAddress(id, address);
    }
    public boolean updatePhoneNumber(String id, String phoneNumber) throws SQLException{
        return userDAO.updateUserPhoneNumber(id, phoneNumber);
    }


    //HELPER
    private static String extractEmail(User user) {
        if (user instanceof Bidder bidder) {
            return bidder.getEmail();
        }
        if (user instanceof Seller seller) {
            return seller.getEmail();
        }
        return null;
    }
}
