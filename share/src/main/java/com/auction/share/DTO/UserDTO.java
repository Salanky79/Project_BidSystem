package com.auction.share.DTO;

import java.io.Serializable;

/** DTO chứa thông tin cơ bản của người dùng. */
public class UserDTO implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String id;
  private final String username;
  private final String fullName;
  private final String role;
  private final String phoneNumber;
  private final String email;
  private final String address;
  private final double balance;

  public UserDTO(
      String id,
      String username,
      String fullName,
      String role,
      String phoneNumber,
      String email,
      String address,
      double balance) {
    this.id = id;
    this.username = username;
    this.fullName = fullName;
    this.role = role;
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.address = address;
    this.balance = balance;
  }

  public String getId() {
    return id;
  }

  public String getUsername() {
    return username;
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

  public double getBalance() {
    return balance;
  }
}
