package com.auction.share.models.user;

import com.auction.share.models.auction.BidTransaction;
import com.auction.share.enums.Role;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Bidder extends User {
    private String phoneNumber;
    private String email;
    private double balance;
    private String address;
    private List<BidTransaction> bidHistory;

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

    public void deductBalance(double amount) {
        this.balance -= amount;
    }

    public void setBalance(double balance){
        this.balance = balance;
    }
    public void deposit(double amount) {
            this.balance += amount;
    }

    public void recordBidHistory(BidTransaction transaction) {
        this.bidHistory.add(transaction);
    }


    @Override
    public void fillPreparedStatement(PreparedStatement ps) throws SQLException {
        ps.setString(5, this.phoneNumber);
        ps.setString(6, this.email);
        ps.setDouble(8, this.balance);
        ps.setString(9, this.address);
        ps.setInt(10, 0);
    }
}

