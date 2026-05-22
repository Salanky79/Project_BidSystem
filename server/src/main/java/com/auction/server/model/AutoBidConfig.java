package com.auction.server.model;

import java.time.LocalDateTime;

public class AutoBidConfig {
    private final String bidderId;
    private final String auctionId;
    private final double maxBid;
    private final double increment;
    private final LocalDateTime registeredAt;

    public AutoBidConfig(String bidderId, String auctionId, double maxBid, double increment, LocalDateTime registeredAt) {
        this.bidderId = bidderId;
        this.auctionId = auctionId;
        this.maxBid = maxBid;
        this.increment = increment;
        this.registeredAt = registeredAt;
    }

    public String getBidderId() {
        return bidderId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public double getMaxBid() {
        return maxBid;
    }

    public double getIncrement() {
        return increment;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
}
