package com.auction.share.models.user;

import com.auction.share.exceptions.InsufficientFundsException;
import com.auction.share.models.auction.BidTransaction;
import com.auction.share.enums.Role;

import java.util.ArrayList;
import java.util.List;

public class Bidder extends User {
    private double balance;
    private String address;
    private List<BidTransaction> bidHistory;

    public Bidder(String username, String password, String fullName, String address) {
        super(username, password, fullName);
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

    public void deductBalance(double amount) throws InsufficientFundsException {
        if (amount > balance) {
            throw new InsufficientFundsException(
                "Không đủ tiền để thanh toán! Số dư: " + balance + "$, yêu cầu: " + amount + "$"
            );
        }
        this.balance -= amount;
    }

    public void setBalance(double balance){
        this.balance = balance;
    }
    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    public void recordBidHistory(BidTransaction transaction) {
        this.bidHistory.add(transaction);
    }
}

