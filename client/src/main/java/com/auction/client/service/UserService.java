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
      throw new ValidationException("Username va Password khong duoc de trong!");
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
      throw new ValidationException("Ho ten khong duoc de trong!");
    }
    if (username == null || username.trim().isEmpty()) {
      throw new ValidationException("Username khong duoc de trong!");
    }
    if (password == null || password.isEmpty()) {
      throw new ValidationException("Password khong duoc de trong!");
    }
    if (role == null || role.trim().isEmpty()) {
      throw new ValidationException("Vai tro khong hop le!");
    }

    if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
      throw new ValidationException("So dien thoai khong duoc de trong!");
    }
    if (email == null || email.trim().isEmpty()) {
      throw new ValidationException("Email khong duoc de trong!");
    }
    if (address == null || address.isBlank()) {
      throw new ValidationException("Dia chi khong hop le!");
    }

    String normalizedPhone = phoneNumber.trim();
    if (!normalizedPhone.matches("^0\\d{8,10}$")) {
      throw new ValidationException("So dien thoai khong hop le!");
    }

    String normalizedEmail = email.trim();
    if (!normalizedEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
      throw new ValidationException("Email khong hop le!");
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
      throw new ValidationException("Ho ten khong duoc de trong!");
    }
    String normalizedPhone =
        (phoneNumber == null || phoneNumber.trim().isEmpty()) ? null : phoneNumber.trim();
    if (normalizedPhone != null && !normalizedPhone.matches("^0\\d{8,10}$")) {
      throw new ValidationException("So dien thoai khong hop le!");
    }

    String normalizedEmail = (email == null || email.trim().isEmpty()) ? null : email.trim();
    if (normalizedEmail != null
        && !normalizedEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
      throw new ValidationException("Email khong hop le!");
    }

    socketClient.send(
        new UpdateProfileRequest(
            null, fullName.trim(), password, normalizedPhone, normalizedEmail, null),
        onResponse);
  }

}
