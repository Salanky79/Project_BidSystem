package com.auction.share.models.user;

import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.enums.Role;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Người bán và các phiên đấu giá do người bán tạo. */
public class Seller extends User {
  private String phoneNumber;
  private String email;
  private String address;
  private double balance;
  private List<String> auctionIdList;

  /** Tạo seller và khởi tạo số dư mặc định. */
  public Seller(
      String username,
      String password,
      String fullName,
      String phoneNumber,
      String email,
      String address) {
    super(username, password, fullName);
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.address = address;
    this.auctionIdList = new ArrayList<>();
    this.balance = 0.0;
    this.setRole(Role.SELLER);
  }

  /** Tạo phiên đấu giá và lưu ID vào danh sách quản lý. */
  public Auction createAuction(Item item, LocalDateTime startTime, LocalDateTime endTime) {
    Auction newAuction = new Auction(item, this, startTime, endTime);
    this.auctionIdList.add(newAuction.getId());
    return newAuction;
  }

  /** Hủy phiên đấu giá và xóa khỏi danh sách quản lý. */
  public void cancelAuction(Auction auction) {
    this.auctionIdList.remove(auction.getId());
  }

  /** Cộng thêm tiền vào số dư. */
  public void addBalance(double amount) {
    this.balance += amount;
  }

  public void setBalance(double balance) {
    this.balance = balance;
  }

  public double getBalance() {
    return balance;
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

  public void setAddress(String address) {
    this.address = address;
  }
}