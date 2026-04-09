package com.auction.share.models.user;

import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.enums.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Seller extends User {
    private double balance;
    private List<String> auctionIdList;

    public Seller(String username, String password, String fullName, String uid) {
        super(username, password, fullName, uid);
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
        if (this.auctionIdList.contains(auction.getId())) {
            this.auctionIdList.remove(auction.getId());
            System.out.println("Đã hủy phiên đấu giá mặt hàng: " + auction.getItem().getName());
        } else {
            System.out.println("Lỗi: Phiên đấu giá này không thuộc về bạn!");
        }
    }

    public void addBalance(double amount) {
        if (amount > 0) {
            this.balance += amount;
            System.out.println("[$] Biến động số dư: Seller [" + this.getUsername() + "] được cộng +" + amount + "$. Số dư hiện tại: " + this.balance + "$");
        } else {
            System.out.println("Lỗi: Số tiền cộng vào phải lớn hơn 0!");
        }
    }

    public double getBalance() {
        return balance;
    }
}

