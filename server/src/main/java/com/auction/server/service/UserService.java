package com.auction.server.service;

import com.auction.server.dao.UserDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.share.DTO.ProfileBidTransactionDTO;
import com.auction.share.DTO.ProfileDTO;
import com.auction.share.DTO.UserDTO;
import com.auction.server.util.PasswordUtil;
import com.auction.share.exceptions.AuthenticationException;
import com.auction.share.exceptions.DuplicateResourceException;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;

import java.sql.SQLException;
import java.util.List;

// Controller → Service → DAO → Database
/**
 * Xử lý nghiệp vụ người dùng trên server.
 */
public class UserService {
    private final UserDAO userDAO;
    private final BidTransactionDAO bidTransactionDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.bidTransactionDAO = new BidTransactionDAO();
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
        // Lưu mật khẩu dạng hash.
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

    // Lấy user theo ID từ database.
    public User getById(String id) throws SQLException, ValidationException {
        User user = userDAO.findById(id);
        if (user == null) {
            throw new ValidationException("User not found.");
        }
        return user;
    }


    // Các hàm cập nhật thông tin người dùng.
    public boolean updatePassword(String id, String password) throws SQLException {
        String hashedPassword = PasswordUtil.hashPassword(password);
        return userDAO.updateUserPassword(id, hashedPassword);
    }

    public boolean updateFullName(String id, String fullName) throws SQLException {
        return userDAO.updateUserFullName(id, fullName);
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

    public ProfileDTO getProfile(String id) throws SQLException, ValidationException {
        User user = getById(id);
        UserDTO userDTO = toUserDTO(user);
        List<ProfileBidTransactionDTO> bidTransactions = bidTransactionDAO.findProfileTransactionsByBidderId(id);
        return new ProfileDTO(userDTO, bidTransactions);
    }

    // Helper.
    private UserDTO toUserDTO(User user) {
        String phoneNumber = null;
        String email = null;
        String address = null;
        double balance = 0.0;

        if (user instanceof Bidder bidder) {
            phoneNumber = bidder.getPhoneNumber();
            email = bidder.getEmail();
            address = bidder.getAddress();
            balance = bidder.getBalance();
        } else if (user instanceof Seller seller) {
            phoneNumber = seller.getPhoneNumber();
            email = seller.getEmail();
            address = seller.getAddress();
            balance = seller.getBalance();
        }

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().name(),
                phoneNumber,
                email,
                address,
                balance
        );
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