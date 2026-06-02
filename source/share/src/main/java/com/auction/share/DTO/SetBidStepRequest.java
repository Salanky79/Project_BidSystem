package com.auction.share.DTO;

/** Yêu cầu thiết lập bước giá (bid step) cho một phiên đấu giá. */
public class SetBidStepRequest extends Request {
  private static final long serialVersionUID = 1L;

  private final String auctionId;
  private final double bidStep;
  private final String sellerId;

  public SetBidStepRequest(String auctionId, double bidStep, String sellerId) {
    super(Action.SET_BID_STEP);
    this.auctionId = auctionId;
    this.bidStep = bidStep;
    this.sellerId = sellerId;
  }

  public String getAuctionId() {
    return auctionId;
  }

  public double getBidStep() {
    return bidStep;
  }

  public String getSellerId() {
    return sellerId;
  }
}
