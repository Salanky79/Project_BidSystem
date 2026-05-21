package com.auction.share.DTO;

import java.io.Serializable;

/**
 * DTO cho một lượt đặt giá.
 */
public class BidDTO implements Serializable {
    // serialVersionUID để giữ tương thích khi serialize/deserialize.
    private static final long serialVersionUID = 1L;

    private final String bidderName;
    private final double amount;
    private final String timestamp;

    public BidDTO(String bidderName, double amount, String timestamp) {
        this.bidderName = bidderName;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public double getAmount() {
        return amount;
    }
}