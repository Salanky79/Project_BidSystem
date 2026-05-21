package com.auction.share.DTO;

import java.io.Serializable;

/**
 * Event cập nhật đặt giá gửi real-time tới client.
 */
public class BidUpdateEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String eventType;
    private final String auctionId;
    private final String bidderId;
    private final String bidderName;
    private final double amount;
    private final double currentHighestBid;
    private final String bidTime;

    public BidUpdateEvent(
            String eventType,
            String auctionId,
            String bidderId,
            String bidderName,
            double amount,
            double currentHighestBid,
            String bidTime
    ) {
        this.eventType = eventType;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidderName = bidderName;
        this.amount = amount;
        this.currentHighestBid = currentHighestBid;
        this.bidTime = bidTime;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public double getAmount() {
        return amount;
    }
}