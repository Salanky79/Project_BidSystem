package com.auction.share.models.user;

import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.enums.Role;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Đại diện cho Người bán (Seller) trong hệ thống.
 * Kế thừa từ User, Seller có quyền tạo các phiên đấu giá cho sản phẩm của mình,
 * quản lý lịch sử các phiên đấu giá và nhận tiền khi phiên đấu giá thành công.
 */
public class Seller extends User {
    /**
     * Số điện thoại liên hệ của người bán.
     */
    private String phoneNumber;

    /**
     * Địa chỉ email của người bán.
     */
    private String email;

    /**
     * Địa chỉ (cửa hàng/kho/cư trú) của người bán.
     */
    private String address;

    /**
     * Số dư tài khoản, nơi lưu trữ tiền nhận được từ các phiên đấu giá thành công.
     */
    private double balance;

    /**
     * Danh sách lưu trữ mã ID của các phiên đấu giá mà người bán đã tạo.
     */
    private List<String> auctionIdList;

    /**
     * Khởi tạo đối tượng Seller.
     * Mặc định vai trò là Role.SELLER, số dư khởi tạo là 0.0.
     *
     * @param username    Tên đăng nhập
     * @param password    Mật khẩu
     * @param fullName    Tên đầy đủ
     * @param phoneNumber Số điện thoại
     * @param email       Địa chỉ email
     * @param address     Địa chỉ liên hệ
     */
    public Seller(String username, String password, String fullName, String phoneNumber, String email, String address) {
        super(username, password, fullName);
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.auctionIdList = new ArrayList<>();
        this.balance = 0.0;
        this.setRole(Role.SELLER);
    }

    /**
     * Tạo một phiên đấu giá mới với sản phẩm và thời gian cấu hình.
     * Đồng thời lưu mã phiên đấu giá vào danh sách của Seller.
     *
     * @param item      Sản phẩm cần đấu giá
     * @param startTime Thời gian bắt đầu
     * @param endTime   Thời gian kết thúc
     * @return Phiên đấu giá mới được tạo
     */
    public Auction createAuction(Item item, LocalDateTime startTime, LocalDateTime endTime) {
        Auction newAuction = new Auction(item, this, startTime, endTime);
        this.auctionIdList.add(newAuction.getId());
        return newAuction;
    }

    /**
     * Hủy bỏ một phiên đấu giá đã tạo (xóa ID khỏi danh sách quản lý).
     *
     * @param auction Phiên đấu giá cần hủy
     */
    public void cancelAuction(Auction auction) {
        this.auctionIdList.remove(auction.getId());
    }


    public void setBalance(double balance){
        this.balance = balance;
    }

    /**
     * Cộng thêm tiền vào số dư tài khoản của người bán.
     *
     * @param amount Số tiền cộng thêm (thường sau khi đấu giá thành công)
     */
    public void addBalance(double amount) {
        this.balance += amount;
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