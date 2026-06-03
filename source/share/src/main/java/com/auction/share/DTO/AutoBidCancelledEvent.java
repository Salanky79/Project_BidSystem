package com.auction.share.DTO;

import java.io.Serializable;

/** Sự kiện thông báo hủy Auto-bid kèm lý do cụ thể. */
public class AutoBidCancelledEvent implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String auctionId;
  private final String reason;

  public AutoBidCancelledEvent(String auctionId, String reason) {
    this.auctionId = auctionId;
    this.reason = reason;
  }

  public String getAuctionId() {
    return auctionId;
  }

  public String getReason() {
    return reason;
  }
}
