package com.auction.server.controller;

import com.auction.server.service.UserService;
import com.auction.share.DTO.GetProfileRequest;
import com.auction.share.DTO.LoginRequest;
import com.auction.share.DTO.ProfileDTO;
import com.auction.share.DTO.RegisterRequest;
import com.auction.share.DTO.Response;
import com.auction.share.DTO.UpdateProfileRequest;
import com.auction.share.DTO.UserDTO;
import com.auction.share.enums.Role;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.user.Admin;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;
import java.sql.SQLException;

/**
 * Controller xử lý các yêu cầu liên quan đến tài khoản người dùng (đăng nhập, đăng ký, cập nhật hồ
 * sơ).
 */
public class UserController {
  private final UserService userService;

  // luồng xử lý: RequestHandler → UserController → UserService → DAO
  public UserController(UserService userService) {
    this.userService = userService;
  }

  public Response<UserDTO> login(LoginRequest request) throws Exception {
    String username = request.getUsername();
    String password = request.getPassword();
    validateRequiredText(username, "Username is required.");
    validateRequiredText(password, "Password is required.");

    // gọi hàm logic nghiệp vụ từ service
    User user = userService.login(username, password);
    return Response.success("Login success.", toUserDTO(user));
  }

  public Response<UserDTO> register(RegisterRequest request) throws Exception {
    User user = toUser(request);
    validateUserForRegister(user);
    userService.register(user);
    return Response.success("Register success.", toUserDTO(user));
  }

  public Response<UserDTO> updateProfile(UpdateProfileRequest request) throws Exception {
    if (request.getUserId() == null || request.getUserId().isBlank()) {
      return Response.fail("User id is required.");
    }

    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      userService.updatePassword(request.getUserId(), request.getPassword());
    }
    if (request.getFullName() != null && !request.getFullName().isBlank()) {
      userService.updateFullName(request.getUserId(), request.getFullName());
    }
    if (request.getEmail() != null && !request.getEmail().isBlank()) {
      userService.updateEmail(request.getUserId(), request.getEmail());
    }
    if (request.getAddress() != null && !request.getAddress().isBlank()) {
      userService.updateAddress(request.getUserId(), request.getAddress());
    }
    if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
      userService.updatePhoneNumber(request.getUserId(), request.getPhoneNumber());
    }

    User user = userService.getById(request.getUserId());
    return Response.success("Profile updated successfully.", toUserDTO(user));
  }

  public Response<ProfileDTO> getProfile(GetProfileRequest request) throws Exception {
    if (request.getUserId() == null || request.getUserId().isBlank()) {
      return Response.fail("User id is required.");
    }
    ProfileDTO profile = userService.getProfile(request.getUserId());
    return Response.success("Profile loaded successfully.", profile);
  }

  // HELPER: mapping request DTO thành entity User trong hệ thống
  private User toUser(RegisterRequest request) {
    Role role = Role.valueOf(request.getRole().trim().toUpperCase());
    // switch expression khởi tạo đối tượng tương ứng theo enum Role
    return switch (role) {
      case BIDDER ->
          new Bidder(
              request.getUsername(),
              request.getPassword(),
              request.getFullName(),
              request.getPhoneNumber(),
              request.getEmail(),
              request.getAddress());
      case SELLER ->
          new Seller(
              request.getUsername(),
              request.getPassword(),
              request.getFullName(),
              request.getPhoneNumber(),
              request.getEmail(),
              request.getAddress());
      case ADMIN ->
          new Admin(request.getUsername(), request.getPassword(), request.getFullName(), 1);
    };
  }

  // HELPER: mapping entity User thành profile DTO để trả về cho client
  private UserDTO toUserDTO(User user) throws SQLException {
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
    double availableBalance = balance;

    return new UserDTO(
        user.getId(),
        user.getUsername(),
        user.getFullName(),
        user.getRole().name(),
        phoneNumber,
        email,
        address,
        balance,
        availableBalance);
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

  private static void validateRequiredText(String value, String message)
      throws ValidationException {
    if (value == null || value.isBlank()) {
      throw new ValidationException(message);
    }
  }

  private static void validatePassword(String password) throws ValidationException {
    validateRequiredText(password, "Password is required.");
    if (password.length() < 6) {
      throw new ValidationException("Password must be at least " + 6 + " characters.");
    }
  }
}
