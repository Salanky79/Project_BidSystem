package com.auction.share.DTO;

public class DepositRequest extends Request {
    private double amount;

    public DepositRequest(String userId, double amount) {
        super(Action.DEPOSIT);
        withUserId(userId);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
