package com.auction.share.DTO;

/** Yêu cầu lấy danh sách các phiên đấu giá. */
public class ListAuctionRequest extends Request {
  private static final long serialVersionUID = 1L;
  private final String status;
  private final String sellerId;
  private final boolean sellerOnly;

  public ListAuctionRequest() {
    this(null, null, false);
  }

  public ListAuctionRequest(String status, String sellerId, boolean sellerOnly) {
    super(Action.LIST_AUCTIONS);
    this.status = status;
    this.sellerId = sellerId;
    this.sellerOnly = sellerOnly;
  }

  public String getStatus() {
    return status;
  }

  public String getSellerId() {
    return sellerId;
  }

  @Override
  public Request withUserId(String userId) {
    // Chỉ tự động điền userId vào sellerId nếu yêu cầu này thuộc về Seller Dashboard (sellerOnly = true)
    if (sellerOnly && (sellerId == null || sellerId.isBlank())) {
      return new ListAuctionRequest(status, userId, true);
    }
    return this;
  }
}
