package com.auction.share.DTO;

import java.io.Serializable;
import java.util.List;

/** Đối tượng truyền dữ liệu (DTO) chứa thông tin chi tiết của một phiên đấu giá. */
public class AuctionDetailDTO implements Serializable {
  private static final long serialVersionUID = 2L;

  private final String auctionId;
  private final String itemName;
  private final String description;
  private final String category;
  private final String sellerName;
  private final double startingPrice;
  private final double currentPrice;
  private final double bidStep;
  private final String status;
  private final String startTime;
  private final String endTime;
  private final String highestBidderName;
  private final String highestBidderUsername;
  private final List<BidDTO> bidHistory;
  private final int bidCount;
  private final String imageUrl;

  public AuctionDetailDTO(
      String auctionId,
      String itemName,
      String description,
      String category,
      String sellerName,
      double startingPrice,
      double currentPrice,
      double bidStep,
      String status,
      String startTime,
      String endTime,
      String highestBidderName,
      String highestBidderUsername,
      List<BidDTO> bidHistory,
      int bidCount,
      String imageUrl) {
    this.auctionId = auctionId;
    this.itemName = itemName;
    this.description = description;
    this.category = category;
    this.sellerName = sellerName;
    this.startingPrice = startingPrice;
    this.currentPrice = currentPrice;
    this.bidStep = bidStep;
    this.status = status;
    this.startTime = startTime;
    this.endTime = endTime;
    this.highestBidderName = highestBidderName;
    this.highestBidderUsername = highestBidderUsername;
    this.bidHistory = bidHistory;
    this.bidCount = bidCount;
    this.imageUrl = imageUrl;
  }


  public String getAuctionId() {
    return auctionId;
  }

  public String getItemName() {
    return itemName;
  }

  public String getDescription() {
    return description;
  }

  public String getCategory() {
    return category;
  }

  public String getSellerName() {
    return sellerName;
  }

  public double getStartingPrice() {
    return startingPrice;
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

  public String getHighestBidderName() {
    return highestBidderName;
  }

  public String getHighestBidderUsername() {
    return highestBidderUsername;
  }

  public List<BidDTO> getBidHistory() {
    return bidHistory;
  }

  public int getBidCount() {
    return bidCount;
  }

  public String getImageUrl() {
    return imageUrl;
  }
}