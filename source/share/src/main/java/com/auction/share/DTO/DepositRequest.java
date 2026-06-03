package com.auction.share.DTO;

public class DepositRequest extends Request {
    private String userId;
    private double amount;

    public DepositRequest(String userId, double amount) {
        super(Action.DEPOSIT);
        this.userId = userId;
        this.amount = amount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
