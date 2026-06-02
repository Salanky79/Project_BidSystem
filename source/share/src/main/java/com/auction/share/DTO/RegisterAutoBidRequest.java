package com.auction.share.DTO;

/** Yêu cầu đăng ký tính năng tự động trả giá (auto-bid) cho một phiên đấu giá. */
public class RegisterAutoBidRequest extends Request {
  private static final long serialVersionUID = 1L;

  private final String auctionId;
  private final double maxBid;
  private final double increment;
  private final String bidderId;

  public RegisterAutoBidRequest(
          String auctionId, double maxBid, double increment, String bidderId) {
    super(Action.REGISTER_AUTO_BID);
    this.auctionId = auctionId;
    this.maxBid = maxBid;
    this.increment = increment;
    this.bidderId = bidderId;
  }

  public String getAuctionId() {
    return auctionId;
  }

  public double getMaxBid() {
    return maxBid;
  }

  public double getIncrement() {
    return increment;
  }

  public String getBidderId() {
    return bidderId;
  }
}
