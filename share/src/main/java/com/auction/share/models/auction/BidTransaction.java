package com.auction.share.models.auction;

import com.auction.share.models.core.Entity;
import com.auction.share.models.user.Bidder;
import java.time.LocalDateTime;

/**
 * Giao dịch trả giá trong phiên đấu giá.
 */
public class BidTransaction extends Entity {
    private final Auction auction;
    private final Bidder bidder;
    private final double amount;
    private LocalDateTime timestamp;

    public BidTransaction(Auction auction, Bidder bidder, double amount) {
        super();
        this.auction = auction;
        this.bidder = bidder;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    public Auction getAuction() { return auction; }
    public Bidder getBidder() { return bidder; }
    public double getAmount() { return amount; }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public LocalDateTime getTimestamp() { return timestamp; }

    /**
     * Chuỗi mô tả giao dịch dùng cho log.
     */
    public String getTransactionDetails() {
        return "Account [" + bidder.getUsername() + "] placed " +
                amount + " VND at " + timestamp;
    }
}