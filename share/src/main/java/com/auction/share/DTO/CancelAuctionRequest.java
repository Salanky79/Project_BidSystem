package com.auction.share.DTO;

public class CancelAuctionRequest extends Request {
    private String auctionId;

    public CancelAuctionRequest() {
        super(Action.CANCEL_AUCTION);
    }

    public CancelAuctionRequest(String auctionId) {
        super(Action.CANCEL_AUCTION);
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }
}
