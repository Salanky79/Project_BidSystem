package com.auction.share.DTO;

public class PlaceBidRequest extends Request {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String bidderId;
    private final double amount;

    public PlaceBidRequest(String auctionId, String bidderId, double amount) {
        super(Action.PLACE_BID);
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public double getAmount() {
        return amount;
    }

    public Request withUserId(String userId) {
        if (this.bidderId == null || this.bidderId.isBlank()) {
            return new PlaceBidRequest(auctionId, userId, amount);
        }
        return this;
    }
}
