package com.auction.share.DTO;

import java.io.Serializable;

/** Sự kiện cập nhật bước giá mới của phiên đấu giá. */
public class BidStepUpdateEvent implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String auctionId;
  private final double bidStep;

  public BidStepUpdateEvent(String auctionId, double bidStep) {
    this.auctionId = auctionId;
    this.bidStep = bidStep;
  }

  public String getAuctionId() {
    return auctionId;
  }

  public double getBidStep() {
    return bidStep;
  }
}
