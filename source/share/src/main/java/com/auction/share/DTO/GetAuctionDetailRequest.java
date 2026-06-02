package com.auction.share.DTO;

/** Yêu cầu lấy thông tin chi tiết của một phiên đấu giá cụ thể. */
public class GetAuctionDetailRequest extends Request {
  private static final long serialVersionUID = 1L;

  private final String auctionId;

  public GetAuctionDetailRequest(String auctionId) {
    super(Action.GET_AUCTION_DETAIL);
    this.auctionId = auctionId;
  }

  public String getAuctionId() {
    return auctionId;
  }
}
