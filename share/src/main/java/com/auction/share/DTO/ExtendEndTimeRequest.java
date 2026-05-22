package com.auction.share.DTO;

/**
 * Yêu cầu gia hạn thời gian kết thúc của phiên đấu giá.
 */
public class ExtendEndTimeRequest extends Request {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final long minutes;
    private final String sellerId;

    public ExtendEndTimeRequest(String auctionId, long minutes, String sellerId) {
        super(Action.EXTEND_END_TIME);
        this.auctionId = auctionId;
        this.minutes = minutes;
        this.sellerId = sellerId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public long getMinutes() {
        return minutes;
    }

    public String getSellerId() {
        return sellerId;
    }

    @Override
    public Request withUserId(String userId) {
        if (this.sellerId == null || this.sellerId.isBlank()) {
            return new ExtendEndTimeRequest(auctionId, minutes, userId);
        }
        return this;
    }
}
