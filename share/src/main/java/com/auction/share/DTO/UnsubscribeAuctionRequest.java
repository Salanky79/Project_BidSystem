package com.auction.share.DTO;

/**
 * Yêu cầu hủy theo dõi một phiên đấu giá.
 */
public class UnsubscribeAuctionRequest extends Request {
    private static final long serialVersionUID = 1L;
    private final String auctionId;

    public UnsubscribeAuctionRequest() {
        this(null);
    }

    public UnsubscribeAuctionRequest(String auctionId) {
        super(Action.UNSUBSCRIBE_AUCTION);
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }
}
