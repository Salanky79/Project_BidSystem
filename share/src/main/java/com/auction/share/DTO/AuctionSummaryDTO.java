package com.auction.share.DTO;

import java.io.Serializable;

public class AuctionSummaryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String itemName;
    private final String category;
    private final double currentPrice;
    private final String status;
    private final String endTime;

    public AuctionSummaryDTO(
            String auctionId,
            String itemName,
            String category,
            double currentPrice,
            String status,
            String endTime
    ) {
        this.auctionId = auctionId;
        this.itemName = itemName;
        this.category = category;
        this.currentPrice = currentPrice;
        this.status = status;
        this.endTime = endTime;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getCategory() {
        return category;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public String getStatus() {
        return status;
    }

    public String getEndTime() {
        return endTime;
    }
}
