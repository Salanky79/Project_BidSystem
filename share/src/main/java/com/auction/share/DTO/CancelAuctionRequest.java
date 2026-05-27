package com.auction.share.DTO;

/** Yêu cầu hủy bỏ một phiên đấu giá. */
public class CancelAuctionRequest extends Request {
  private String auctionId;


  public CancelAuctionRequest(String auctionId) {
    super(Action.CANCEL_AUCTION);
    this.auctionId = auctionId;
  }

  public String getAuctionId() {
    return auctionId;
  }

}
