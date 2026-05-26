package com.auction.share.DTO;

/** Yêu cầu hủy bỏ tính năng tự động trả giá (auto-bid). */
public class CancelAutoBidRequest extends Request {
  private static final long serialVersionUID = 1L;

  private final String auctionId;
  private final String bidderId;

  public CancelAutoBidRequest(String auctionId, String bidderId) {
    super(Action.CANCEL_AUTO_BID);
    this.auctionId = auctionId;
    this.bidderId = bidderId;
  }

  public String getAuctionId() {
    return auctionId;
  }

  public String getBidderId() {
    return bidderId;
  }

  @Override
  public Request withUserId(String userId) {
    if (userId == null || userId.isBlank()) {
      return this;
    }
    // Always trust server-side session userId, never client-supplied bidderId.
    return new CancelAutoBidRequest(auctionId, userId);
  }
}
