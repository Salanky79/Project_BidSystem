package com.auction.server.service;

import com.auction.share.models.user.Bidder;

class AutoBidOrder {
    private final String auctionId;
    private final Bidder bidder;
    private final double maxBid;
    private final double increment;
    // thứ tự ưu tiên (khi cùng maxBid)
    private final long sequence;
    private boolean active = true;

    AutoBidOrder(String auctionId, Bidder bidder, double maxBid, double increment, long sequence) {
        // lưu Bidder => đỡ phải query DB nhiều => truy cập qua Bidder
        this.auctionId = auctionId;
        this.bidder = bidder;
        this.maxBid = maxBid;
        this.increment = increment;
        this.sequence = sequence;
    }

    String getAuctionId() {
        return auctionId;
    }

    Bidder getBidder() {
        return bidder;
    }

    double getMaxBid() {
        return maxBid;
    }

    double getIncrement() {
        return increment;
    }

    long getSequence() {
        return sequence;
    }

    boolean isActive() {
        return active;
    }

    void deactivate() {
        active = false;
    }
}
