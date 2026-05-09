package com.auction.share.DTO;

import java.io.Serializable;

public class BidDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String bidderName;
    private final double amount;
    private final String timestamp;

    public BidDTO(String bidderName, double amount, String timestamp) {
        this.bidderName = bidderName;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getBidderName() {
        return bidderName;
    }

    public double getAmount() {
        return amount;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
