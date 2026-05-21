package com.auction.share.models.user;

import com.auction.share.models.auction.BidTransaction;
import com.auction.share.enums.Role;
import java.util.ArrayList;
import java.util.List;

/**
 * Đại diện cho Người tham gia đấu giá (Bidder).
 * Kế thừa từ lớp User, người này có thể nạp tiền, đặt bid và lưu trữ lịch sử các lần trả giá.
 */
public class Bidder extends User {
    /**
     * Số điện thoại liên hệ của người đấu giá.
     */
    private String phoneNumber;

    /**
     * Địa chỉ email của người đấu giá.
     */
    private String email;

    /**
     * Số dư tài khoản hiện tại, dùng để thanh toán hoặc đặt cọc khi tham gia đấu giá.
     */
    private double balance;

    /**
     * Địa chỉ cư trú hoặc nhận hàng của người đấu giá.
     */
    private String address;

    /**
     * Lịch sử các giao dịch trả giá đã thực hiện.
     */
    private List<BidTransaction> bidHistory;

    /**
     * Khởi tạo đối tượng Bidder.
     * Mặc định vai trò là Role.BIDDER, số dư mốc ban đầu (balance) là 0.0 và khởi tạo danh sách lịch sử trống.
     *
     * @param username     Tên đăng nhập
     * @param password     Mật khẩu
     * @param fullName     Tên đầy đủ
     * @param phoneNumber  Số điện thoại
     * @param email        Địa chỉ email
     * @param address      Địa chỉ liên hệ
     */
    public Bidder(String username, String password, String fullName,String phoneNumber, String email, String address) {
        super(username, password, fullName);
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.balance = 0.0;
        this.bidHistory = new ArrayList<>();
        this.setRole(Role.BIDDER);
    }

    public String getName() { 
        return this.getFullName();
    }
    public String getAddress(){
        return address;
    }
    public double getBalance() { 
        return balance; 
    }
    public String getPhoneNumber(){
        return phoneNumber;
    }
    public String getEmail(){
        return email;
    }

    /**
     * Khấu trừ tiền từ số dư tài khoản.
     * Thường dùng khi thanh toán hoặc khi bị trừ tiền cọc.
     *
     * @param amount Số tiền cần trừ
     */
    public void deductBalance(double amount) {
        this.balance -= amount;
    }

    public void setBalance(double balance){
        this.balance = balance;
    }

    /**
     * Nạp tiền vào tài khoản để tăng số dư.
     *
     * @param amount Số tiền nạp vào
     */
    public void deposit(double amount) {
            this.balance += amount;
    }

    /**
     * Ghi nhận một phiên giao dịch trả giá vào lịch sử của Bidder.
     *
     * @param transaction Giao dịch trả giá cần lưu
     */
    public void recordBidHistory(BidTransaction transaction) {
        this.bidHistory.add(transaction);
    }
}