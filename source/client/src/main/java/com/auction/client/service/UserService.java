package com.auction.client.service;

import com.auction.client.network.SocketClient;
import com.auction.share.DTO.GetProfileRequest;
import com.auction.share.DTO.LoginRequest;
import com.auction.share.DTO.RegisterRequest;
import com.auction.share.DTO.Response;
import com.auction.share.DTO.UpdateProfileRequest;
import com.auction.share.DTO.UserDTO;
import com.auction.share.exceptions.ValidationException;
import java.util.function.Consumer;

public class UserService {
  private final SocketClient socketClient;

  public UserService(SocketClient socketClient) {
    this.socketClient = socketClient;
  }

  // Consumer cho phép nhận vào biến chứa hàm (lambda ->///)
  // onResponse =
  // response -> {
  //    System.out.println(response);
  // }
  // onResponse là callback chạy từ login controller
  // async + callback => tránh tình trạng UI bị đơ
  public void login(String username, String password, Consumer<Response<?>> onResponse)
      throws ValidationException {
    if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
      throw new ValidationException("Username and password cannot be blank!");
    }

    LoginRequest request = new LoginRequest(username, password);
    socketClient.send(request, onResponse);
  }

  public void signup(
      String username,
      String password,
      String fullName,
      String role,
      String phoneNumber,
      String email,
      String address,
      Consumer<Response<?>> onResponse)
      throws ValidationException {

    if (fullName == null || fullName.trim().isEmpty()) {
      throw new ValidationException("Full name cannot be blank!");
    }
    if (username == null || username.trim().isEmpty()) {
      throw new ValidationException("Username cannot be blank!");
    }
    if (password == null || password.isEmpty()) {
      throw new ValidationException("Password cannot be blank!");
    }
    if (role == null || role.trim().isEmpty()) {
      throw new ValidationException("Invalid role!");
    }

    if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
      throw new ValidationException("Phone number cannot be blank!");
    }
    if (email == null || email.trim().isEmpty()) {
      throw new ValidationException("Email cannot be blank!");
    }
    if (address == null || address.isBlank()) {
      throw new ValidationException("Invalid address!");
    }

    String normalizedPhone = phoneNumber.trim();
    if (!normalizedPhone.matches("^0\\d{8,10}$")) {
      throw new ValidationException("Invalid phone number!");
    }

    String normalizedEmail = email.trim();
    if (!normalizedEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
      throw new ValidationException("Invalid email!");
    }

    RegisterRequest request =
        new RegisterRequest(
            username.trim(), password, fullName.trim(), role.trim(), normalizedPhone, normalizedEmail, address.trim());
    socketClient.send(request, onResponse);
  }

  public void getProfile(Consumer<Response<?>> onResponse) {
    socketClient.send(new GetProfileRequest(null), onResponse);
  }

  public void updateProfile(
      String fullName, String phoneNumber, String email, String password, Consumer<Response<?>> onResponse)
      throws ValidationException {
    if (fullName == null || fullName.trim().isEmpty()) {
      throw new ValidationException("Full name cannot be blank!");
    }
    String normalizedPhone =
        (phoneNumber == null || phoneNumber.trim().isEmpty()) ? null : phoneNumber.trim();
    if (normalizedPhone != null && !normalizedPhone.matches("^0\\d{8,10}$")) {
      throw new ValidationException("Invalid phone number!");
    }

    String normalizedEmail = (email == null || email.trim().isEmpty()) ? null : email.trim();
    if (normalizedEmail != null
        && !normalizedEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
      throw new ValidationException("Invalid email!");
    }

    socketClient.send(
        new UpdateProfileRequest(
            null, fullName.trim(), password, normalizedPhone, normalizedEmail, null),
        onResponse);
  }

}
