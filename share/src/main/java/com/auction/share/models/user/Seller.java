package com.auction.share.models.user;

import com.auction.share.enums.Role;
import java.util.ArrayList;
import java.util.List;

/**
 * Người bán trong hệ thống.
 */
public class Seller extends User {
    private String phoneNumber;
    private String email;
    private String address;
    private double balance;
    private List<String> auctionIdList;

    public Seller(String username, String password, String fullName, String phoneNumber, String email, String address) {
        super(username, password, fullName);
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.auctionIdList = new ArrayList<>();
        this.balance = 0.0;
        this.setRole(Role.SELLER);
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