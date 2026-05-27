package com.auction.share.DTO;

/** Yêu cầu lấy danh sách các phiên đấu giá. */
public class ListAuctionRequest extends Request {
  private static final long serialVersionUID = 1L;
  private final String status;
  private final String sellerId;

  public ListAuctionRequest() {
    this(null, null);
  }

  public ListAuctionRequest(String status) {
    this(status, null);
  }

  public ListAuctionRequest(String status, String sellerId) {
    super(Action.LIST_AUCTIONS);
    this.status = status;
    this.sellerId = sellerId;
  }

  public String getStatus() {
    return status;
  }

  public String getSellerId() {
    return sellerId;
  }

  @Override
  public Request withUserId(String userId) {
    // Nếu đã có sellerId (client truyền) thì giữ nguyên.
    // Nếu chưa có → server inject từ session (dùng cho trang "My Auctions" của Seller).
    if (sellerId != null && !sellerId.isBlank()) {
      return this;
    }
    if (userId == null || userId.isBlank()) {
      return this;
    }
    return new ListAuctionRequest(status, userId);
  }
}
