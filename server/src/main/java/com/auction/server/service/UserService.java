package com.auction.server.service;

import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.mapper.UserMapper;
import javax.sql.DataSource;
import java.sql.Connection;
import com.auction.server.util.PasswordUtil;
import com.auction.share.DTO.ProfileBidTransactionDTO;
import com.auction.share.DTO.ProfileDTO;
import com.auction.share.DTO.UpdateProfileRequest;
import com.auction.share.DTO.UserDTO;
import com.auction.share.exceptions.AuthenticationException;
import com.auction.share.exceptions.DuplicateResourceException;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

// luồng xử lý: Controller → Service → DAO → Database

/** Dịch vụ xử lý nghiệp vụ liên quan đến người dùng (đăng nhập, đăng ký, cập nhật thông tin). */
public class UserService implements IUserService {
  private final DataSource dataSource;
  private final UserDAO userDAO;
  private final BidTransactionDAO bidTransactionDAO;
  private final AuctionDAO auctionDAO;
  private final UserMapper userMapper;

  public UserService(DataSource dataSource, UserDAO userDAO, BidTransactionDAO bidTransactionDAO, AuctionDAO auctionDAO, UserMapper userMapper) {
    this.dataSource = dataSource;
    this.userDAO = userDAO;
    this.bidTransactionDAO = bidTransactionDAO;
    this.auctionDAO = auctionDAO;
    this.userMapper = userMapper;
  }

  public boolean register(User user)
      throws SQLException, ValidationException, DuplicateResourceException {
    try (Connection conn = dataSource.getConnection()) {
      if (userDAO.isUsernameTaken(conn, user.getUsername())) {
        throw new DuplicateResourceException("Username already exists.");
      }

      String email = extractEmail(user);
      if (email != null && userDAO.isEmailTaken(conn, email)) {
        throw new DuplicateResourceException("Email already exists.");
      }

      user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
      return userDAO.saveUser(conn, user);
    }
  }

  public User login(String username, String password)
      throws SQLException, AuthenticationException, ValidationException {
    try (Connection conn = dataSource.getConnection()) {
      User user = userDAO.findByUsername(conn, username);
      if (user == null) {
        throw new AuthenticationException("Account does not exist.");
      }
      if (!PasswordUtil.checkPassword(password, user.getPassword())) {
        throw new AuthenticationException("Incorrect password.");
      }
      return user;
    }
  }


  public User updateProfile(UpdateProfileRequest request) throws SQLException, ValidationException {
    try (Connection conn = dataSource.getConnection()) {
      conn.setAutoCommit(false);
      try {
          String hashedPassword = null;
          if (request.getPassword() != null && !request.getPassword().isBlank()) {
              hashedPassword = PasswordUtil.hashPassword(request.getPassword());
          }
          userDAO.updateProfile(conn, request.getUserId(), request.getFullName(), request.getEmail(), request.getAddress(), request.getPhoneNumber(), hashedPassword);
          
          User user = userDAO.findById(conn, request.getUserId());
          if (user == null) {
              throw new ValidationException("User not found.");
          }
          conn.commit();
          return user;
      } catch (SQLException | ValidationException e) {
          conn.rollback();
          throw e;
      } finally {
          conn.setAutoCommit(true);
      }
    }
  }

  public ProfileDTO getProfile(String id) throws SQLException, ValidationException {
    try (Connection conn = dataSource.getConnection()) {
      User user = userDAO.findById(conn, id);
      if (user == null) {
        throw new ValidationException("User not found.");
      }
      UserDTO baseUserDTO = userMapper.fromUserToUserDTO(user);
      double availableBalance =
          baseUserDTO.getBalance()
              - auctionDAO.sumAuctionCurrentPrices(conn, user.getId(), null);
      UserDTO userDTO =
          new UserDTO(
              baseUserDTO.getId(),
              baseUserDTO.getUsername(),
              baseUserDTO.getFullName(),
              baseUserDTO.getRole(),
              baseUserDTO.getPhoneNumber(),
              baseUserDTO.getEmail(),
              baseUserDTO.getAddress(),
              baseUserDTO.getBalance(),
              availableBalance);
      List<ProfileBidTransactionDTO> bidTransactions =
          bidTransactionDAO.findProfileTransactionsByBidderId(conn, id);
      return new ProfileDTO(userDTO, bidTransactions);
    }
  }

  // HELPER: các hàm hỗ trợ chuyển đổi dữ liệu
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
