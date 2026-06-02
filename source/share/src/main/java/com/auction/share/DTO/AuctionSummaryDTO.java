package com.auction.share.DTO;

import java.io.Serializable;

/** Đối tượng truyền dữ liệu (DTO) chứa thông tin tóm tắt của một phiên đấu giá. */
public class AuctionSummaryDTO implements Serializable {
  private static final long serialVersionUID = 3L;

  private final String auctionId;
  private final String itemName;
  private final String category;
  private final double currentPrice;
  private final double bidStep;
  private final String status;
  private final String startTime;
  private final String endTime;
  private final int bidCount;
  private final String imageUrl;
  private final String highestBidderName;

  public AuctionSummaryDTO(
      String auctionId,
      String itemName,
      String category,
      double currentPrice,
      double bidStep,
      String status,
      String startTime,
      String endTime,
      int bidCount,
      String imageUrl,
      String highestBidderName) {
    this.auctionId = auctionId;
    this.itemName = itemName;
    this.category = category;
    this.currentPrice = currentPrice;
    this.bidStep = bidStep;
    this.status = status;
    this.startTime = startTime;
    this.endTime = endTime;
    this.bidCount = bidCount;
    this.imageUrl = imageUrl;
    this.highestBidderName = highestBidderName;
  }


  public String getAuctionId() {
    return auctionId;
  }

  public String getItemName() {
    return itemName;
  }

  public String getCategory() {
    return category;
  }

  public double getCurrentPrice() {
    return currentPrice;
  }

  public double getBidStep() {
    return bidStep;
  }

  public String getStatus() {
    return status;
  }

  public String getStartTime() {
    return startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public int getBidCount() {
    return bidCount;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getHighestBidderName() {
    return highestBidderName;
  }
}