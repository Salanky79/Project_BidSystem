package com.auction.share.DTO;

/**
 * Request hủy theo dõi một phiên đấu giá.
 */
public class UnsubscribeAuctionRequest extends Request {
    private static final long serialVersionUID = 1L;

    public UnsubscribeAuctionRequest(String auctionId) {
        super(Action.UNSUBSCRIBE_AUCTION);
    }
}