package com.auction.share.DTO;

public class ListAuctionRequest extends Request {
    private static final long serialVersionUID = 1L;

    public ListAuctionRequest() {
        super(Action.LIST_AUCTIONS);
    }
}
