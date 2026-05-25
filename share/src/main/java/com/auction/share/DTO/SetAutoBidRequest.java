package com.auction.share.DTO;

/** Yêu cầu thiết lập hoặc cấu hình tính năng tự động trả giá (auto-bid). */
public class SetAutoBidRequest extends Request {
  private static final long serialVersionUID = 1L;

  private final String auctionId;
  private final String bidderId;
  private final double maxBid;
  private final double increment;

  public SetAutoBidRequest(String auctionId, String bidderId, double maxBid, double increment) {
    super(Action.SET_AUTO_BID);
    this.auctionId = auctionId;
    this.bidderId = bidderId;
    this.maxBid = maxBid;
    this.increment = increment;
  }

  public String getAuctionId() {
    return auctionId;
  }

  public String getBidderId() {
    return bidderId;
  }

  public double getMaxBid() {
    return maxBid;
  }

  public double getIncrement() {
    return increment;
  }

  @Override
  public Request withUserId(String userId) {
    if (this.bidderId == null || this.bidderId.isBlank()) {
      return new SetAutoBidRequest(auctionId, userId, maxBid, increment);
    }
    return this;
  }
}
