package com.auction.server.dao;

import com.auction.share.models.user.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

public interface UserDAO {
    boolean saveUser(Connection conn, User user) throws SQLException;
    boolean isUsernameTaken(Connection conn, String username) throws SQLException;
    User findById(Connection conn, String id) throws SQLException;
    User findByUsername(Connection conn, String username) throws SQLException;
    boolean isEmailTaken(Connection conn, String email) throws SQLException;
    boolean updateProfile(Connection conn, String userId, String fullName, String email, String address, String phoneNumber, String password) throws SQLException;

    double findBalanceForUpdate(Connection conn, String userId) throws SQLException;
    int deductWinningBidders(Connection conn, Timestamp endTime) throws SQLException;
    int creditSellers(Connection conn, Timestamp endTime) throws SQLException;
}
