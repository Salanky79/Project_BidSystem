package com.auction.client.service;

public class AutoBidConfig {
    private final double maxBid;
    private final double increment;
    private final boolean active;

    public AutoBidConfig(double maxBid, double increment, boolean active) {
        this.maxBid = maxBid;
        this.increment = increment;
        this.active = active;
    }

    public double getMaxBid() {
        return maxBid;
    }

    public double getIncrement() {
        return increment;
    }

    public boolean isActive() {
        return active;
    }
}
