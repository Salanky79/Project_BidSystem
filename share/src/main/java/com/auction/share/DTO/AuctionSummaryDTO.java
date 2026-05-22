package com.auction.share.DTO;

import java.io.Serializable;

public class AuctionSummaryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String itemName;
    private final String category;
    private final double currentPrice;
    private final String status;
    private final String startTime;
    private final String endTime;
    private final double bidstep;

    public AuctionSummaryDTO(
            String auctionId,
            String itemName,
            String category,
            double currentPrice,
            String status,
            String startTime,
            String endTime,
            double bidstep
    ) {
        this.auctionId = auctionId;
        this.itemName = itemName;
        this.category = category;
        this.currentPrice = currentPrice;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bidstep = bidstep;
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

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public double getBidStep() {
        return bidstep;
    }
}
