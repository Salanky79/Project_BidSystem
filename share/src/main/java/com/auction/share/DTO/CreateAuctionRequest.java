package com.auction.share.DTO;

/** Yêu cầu tạo mới một phiên đấu giá. */
public class CreateAuctionRequest extends Request {
  private static final long serialVersionUID = 1L;

  private final String sellerId;
  private final String itemName;
  private final String description;
  private final String category;
  private final double startingPrice;
  private final String startTime;
  private final String endTime;
  private final byte[] imageBytes;
  private final String imageName;

  public CreateAuctionRequest(
      String sellerId,
      String itemName,
      String description,
      String category,
      double startingPrice,
      String startTime,
      String endTime) {
    this(sellerId, itemName, description, category, startingPrice, startTime, endTime, null, null);
  }

  public CreateAuctionRequest(
      String sellerId,
      String itemName,
      String description,
      String category,
      double startingPrice,
      String startTime,
      String endTime,
      byte[] imageBytes,
      String imageName) {
    super(Action.CREATE_AUCTION);
    this.sellerId = sellerId;
    this.itemName = itemName;
    this.description = description;
    this.category = category;
    this.startingPrice = startingPrice;
    this.startTime = startTime;
    this.endTime = endTime;
    this.imageBytes = imageBytes;
    this.imageName = imageName;
  }

  public String getSellerId() {
    return sellerId;
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

  public double getStartingPrice() {
    return startingPrice;
  }

  public String getStartTime() {
    return startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public byte[] getImageBytes() {
    return imageBytes;
  }

  public String getImageName() {
    return imageName;
  }
}
