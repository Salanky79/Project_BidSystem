package com.auction.share.DTO;

/** Yêu cầu đăng ký tài khoản mới. */
public class RegisterRequest extends Request {
  private static final long serialVersionUID = 1L;

  private final String username;
  private final String password;
  private final String fullName;
  private final String role;
  private final String phoneNumber;
  private final String email;
  private final String address;

  public RegisterRequest(
      String username,
      String password,
      String fullName,
      String role,
      String phoneNumber,
      String email,
      String address) {
    super(Action.REGISTER);
    this.username = username;
    this.password = password;
    this.fullName = fullName;
    this.role = role;
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.address = address;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getFullName() {
    return fullName;
  }

  public String getRole() {
    return role;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public String getEmail() {
    return email;
  }

  public String getAddress() {
    return address;
  }
}
