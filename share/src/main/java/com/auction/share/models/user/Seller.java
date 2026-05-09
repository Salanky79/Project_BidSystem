package com.auction.share.models.user;

import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.enums.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public Auction createAuction(Item item, LocalDateTime startTime, LocalDateTime endTime) {
        Auction newAuction = new Auction(item, this, startTime, endTime);
        this.auctionIdList.add(newAuction.getId());
        return newAuction;
    }

    public void cancelAuction(Auction auction) {
        this.auctionIdList.remove(auction.getId());
    }


    public void setBalance(double balance){
        this.balance = balance;
    }
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

