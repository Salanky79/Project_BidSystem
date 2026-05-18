package com.auction.share.DTO;

import java.io.Serializable;

public class ProfileBidTransactionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String itemName;
    private final String status;
    private final double bidAmount;
    private final String timestamp;

    public ProfileBidTransactionDTO(String itemName, String status, double bidAmount, String timestamp) {
        this.itemName = itemName;
        this.status = status;
        this.bidAmount = bidAmount;
        this.timestamp = timestamp;
    }

    public String getItemName() {
        return itemName;
    }

    public String getStatus() {
        return status;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
