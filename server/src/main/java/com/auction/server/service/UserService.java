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

public class UserService {
    private static final int MIN_PASSWORD_LENGTH = 6;
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean register(User user) throws SQLException, ValidationException, DuplicateResourceException {
        validateUserForRegister(user);

        if (userDAO.isUsernameTaken(user.getUsername())) {
            throw new DuplicateResourceException("Username already exists.");
        }

        String email = extractEmail(user);
        if (email != null && userDAO.isEmailTaken(email)) {
            throw new DuplicateResourceException("Email already exists.");
        }

        user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
        return userDAO.saveUser(user);
    }

    public User login(String username, String password) throws SQLException, AuthenticationException, ValidationException {
        validateRequiredText(username, "Username is required.");
        validateRequiredText(password, "Password is required.");

        User user = userDAO.findByUsername(username);
        if (user == null) {
            throw new AuthenticationException("Account does not exist.");
        }
        if (!PasswordUtil.checkPassword(password, user.getPassword())) {
            throw new AuthenticationException("Incorrect password.");
        }
        return user;
    }

    public boolean updatePassword(String id, String password) throws SQLException, ValidationException {
        validateRequiredText(id, "User id is required.");
        validatePassword(password);

        String hashedPassword = PasswordUtil.hashPassword(password);
        return userDAO.updateUserPassword(id, hashedPassword);
    }

    public boolean updateEmail(String id, String email) throws SQLException, ValidationException, DuplicateResourceException {
        validateRequiredText(id, "User id is required.");
        validateRequiredText(email, "Email is required.");

        User user = userDAO.findById(id);
        if (user == null) {
            throw new ValidationException("User not found.");
        }

        String normalizedEmail = email.trim();
        String currentEmail = extractEmail(user);
        if (currentEmail == null) {
            throw new ValidationException("This account does not support email update.");
        }
        if (currentEmail.equalsIgnoreCase(normalizedEmail)) {
            return true;
        }
        if (userDAO.isEmailTaken(normalizedEmail)) {
            throw new DuplicateResourceException("Email already exists.");
        }

        return userDAO.updateUserEmail(id, normalizedEmail);
    }

    public boolean updateAddress(String id, String address) throws SQLException, ValidationException {
        validateRequiredText(id, "User id is required.");
        validateRequiredText(address, "Address is required.");

        return userDAO.updateUserAddress(id, address);
    }

    private static void validateUserForRegister(User user) throws ValidationException {
        if (user == null) {
            throw new ValidationException("User is required.");
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
                validateRequiredText(seller.getAddress(), "Address is required for seller accounts.");
                break;
            case ADMIN:
                break;
            default:
                throw new ValidationException("Invalid role.");
        }
    }

    private static void validatePassword(String password) throws ValidationException {
        validateRequiredText(password, "Password is required.");
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters."
            );
        }
    }

    private static void validateRequiredText(String value, String message) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
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
