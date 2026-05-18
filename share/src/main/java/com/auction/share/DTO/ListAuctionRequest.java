package com.auction.share.DTO;

public class ListAuctionRequest extends Request {
    private static final long serialVersionUID = 1L;
    private final String status;

    public ListAuctionRequest() {
        this(null);
    }

    public ListAuctionRequest(String status) {
        super(Action.LIST_AUCTIONS);
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public Request withUserId(String userId) {
        return this;
    }
}
