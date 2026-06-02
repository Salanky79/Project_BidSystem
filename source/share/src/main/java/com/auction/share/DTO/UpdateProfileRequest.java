package com.auction.share.DTO;

/** Yêu cầu cập nhật thông tin hồ sơ (profile) của người dùng. */
public class UpdateProfileRequest extends Request {
  private static final long serialVersionUID = 1L;

  private final String fullName;
  private final String password;
  private final String phoneNumber;
  private final String email;
  private final String address;

  public UpdateProfileRequest(
      String userId,
      String fullName,
      String password,
      String phoneNumber,
      String email,
      String address) {
    super(Action.UPDATE_PROFILE);
    withUserId(userId);
    this.fullName = fullName;
    this.password = password;
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.address = address;
  }

  public String getFullName() {
    return fullName;
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

  public String getPassword() {
    return password;
  }
}
