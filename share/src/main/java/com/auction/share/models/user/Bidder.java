package com.auction.share.models.user;

import com.auction.share.models.auction.BidTransaction;
import com.auction.share.enums.Role;
import java.util.ArrayList;
import java.util.List;

/**
 * Người tham gia đấu giá và lịch sử trả giá.
 */
public class Bidder extends User {
    private String phoneNumber;
    private String email;
    private double balance;
    private String address;
    private List<BidTransaction> bidHistory;

    /**
     * Tạo bidder và khởi tạo số dư mặc định.
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

    /**
     * Trừ số dư tài khoản.
     */
    public void deductBalance(double amount) {
        this.balance -= amount;
    }

    /**
     * Nạp tiền vào tài khoản.
     */
    public void deposit(double amount) {
            this.balance += amount;
    }

    /**
     * Lưu giao dịch trả giá vào lịch sử.
     */
    public void recordBidHistory(BidTransaction transaction) {
        this.bidHistory.add(transaction);
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
    public void setBalance(double balance){
        this.balance = balance;
    }
}