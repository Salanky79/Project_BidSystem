package com.auction.server.service;

import com.auction.server.dao.UserDAO;
import com.auction.server.util.PasswordUtil;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;

import java.sql.SQLException;

public class UserService {
    private static final int MIN_PASSWORD_LENGTH = 6;

    public static boolean register(User user) throws SQLException {
        validateUserForRegister(user);

        if (UserDAO.isUsernameTaken(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }

        String email = extractEmail(user);
        if (email != null && UserDAO.isEmailTaken(email)) {
            throw new IllegalArgumentException("Email already exists.");
        }

        user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
        return UserDAO.saveUser(user);
    }

    public static User login(String username, String password) throws SQLException {
        validateRequiredText(username, "Username is required.");
        validateRequiredText(password, "Password is required.");

        User user = UserDAO.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Account does not exist.");
        }
        if (!PasswordUtil.checkPassword(password, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect password.");
        }
        return user;
    }

    public static boolean updatePassword(String id, String password) throws SQLException {
        validateRequiredText(id, "User id is required.");
        validatePassword(password);

        String hashedPassword = PasswordUtil.hashPassword(password);
        return UserDAO.updateUserPassword(id, hashedPassword);
    }

    public static boolean updateAddress(String id, String address) throws SQLException {
        validateRequiredText(id, "User id is required.");
        validateRequiredText(address, "Address is required.");

        return UserDAO.updateUserAddress(id, address);
    }

    private static void validateUserForRegister(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }

        validateRequiredText(user.getUsername(), "Username is required.");
        validateRequiredText(user.getFullName(), "Full name is required.");
        validatePassword(user.getPassword());

        switch (user.getRole()) {
            case BIDDER:
                Bidder bidder = (Bidder) user;
                validateRequiredText(bidder.getPhoneNumber(), "Phone number is required.");
                validateRequiredText(bidder.getEmail(), "Email is required.");
                validateRequiredText(bidder.getAddress(), "Address is required for bidder accounts.");
                break;
            case SELLER:
                Seller seller = (Seller) user;
                validateRequiredText(seller.getPhoneNumber(), "Phone number is required.");
                validateRequiredText(seller.getEmail(), "Email is required.");
                break;
            case ADMIN:
                break;
            default:
                throw new IllegalArgumentException("Invalid role.");
        }
    }

    private static void validatePassword(String password) {
        validateRequiredText(password, "Password is required.");
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters."
            );
        }
    }

    private static void validateRequiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

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
