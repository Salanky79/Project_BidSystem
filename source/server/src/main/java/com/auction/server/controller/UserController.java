package com.auction.server.controller;

import com.auction.server.mapper.UserMapper;
import com.auction.server.service.UserService;
import com.auction.share.DTO.GetProfileRequest;
import com.auction.share.DTO.LoginRequest;
import com.auction.share.DTO.ProfileDTO;
import com.auction.share.DTO.RegisterRequest;
import com.auction.share.DTO.Response;
import com.auction.share.DTO.UpdateProfileRequest;
import com.auction.share.DTO.UserDTO;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;
import com.auction.server.network.ClientSession;
import com.auction.share.DTO.DepositRequest;

/**
 * Controller xử lý các yêu cầu liên quan đến tài khoản người dùng (đăng nhập, đăng ký, cập nhật hồ
 * sơ).
 */
public class UserController {
  private static final int MIN_PASSWORD_LENGTH = 6;
  private final UserService userService;
  private final UserMapper mapUserDTO;

  public UserController(UserService userService, UserMapper mapUserDTO) {
    this.userService = userService;
    this.mapUserDTO = mapUserDTO;
  }

  public Response<UserDTO> login(LoginRequest request) throws Exception {
    String username = request.getUsername();
    String password = request.getPassword();
    validateRequiredText(username, "Username is required.");
    validateRequiredText(password, "Password is required.");

    // gọi hàm logic nghiệp vụ từ service
    User user = userService.login(username, password);
    return Response.success("Login success.", mapUserDTO.fromUserToUserDTO(user));
  }

  public Response<UserDTO> register(RegisterRequest request) throws Exception {
    User user = mapUserDTO.fromRequestToUser(request);
    validateUserForRegister(user);
    userService.register(user);
    return Response.success("Register success.", mapUserDTO.fromUserToUserDTO(user));
  }

  public Response<UserDTO> updateProfile(UpdateProfileRequest request) throws Exception {
    if (request.getUserId() == null || request.getUserId().isBlank()) {
      return Response.fail("User id is required.");
    }

    User user = userService.updateProfile(request);
    return Response.success("Profile updated successfully.", mapUserDTO.fromUserToUserDTO(user));
  }

  public Response<ProfileDTO> getProfile(GetProfileRequest request) throws Exception {
    if (request.getUserId() == null || request.getUserId().isBlank()) {
      return Response.fail("User id is required.");
    }
    ProfileDTO profile = userService.getProfile(request.getUserId());
    return Response.success("Profile loaded successfully.", profile);
  }

  public Response<UserDTO> deposit(DepositRequest request) throws Exception {
    if (request == null || request.getUserId() == null) {
      return Response.fail("Unauthorized.");
    }
    UserDTO updatedUser = userService.deposit(request.getUserId(), request.getAmount());
    return Response.success("Deposit successful.", updatedUser);
  }

  // HELPER
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
    if (password.length() < MIN_PASSWORD_LENGTH) {
      throw new ValidationException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
    }
  }
}

