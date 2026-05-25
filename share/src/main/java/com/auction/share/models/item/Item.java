package com.auction.share.models.item;

import com.auction.share.enums.Category;
import com.auction.share.models.core.Entity;

/** Mặt hàng được đưa vào phiên đấu giá. */
public class Item extends Entity {
  private String name;
  private String description = "";
  private double startingPrice;
  private String sellerId;
  private final Category category;

  /** Tạo mặt hàng với thông tin cơ bản. */
  public Item(
      String name, String description, double startingPrice, String sellerId, Category category) {
    super();
    this.name = name;
    this.description = description;
    this.startingPrice = startingPrice;
    this.sellerId = sellerId;
    this.category = category;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public double getStartingPrice() {
    return startingPrice;
  }

  public String getSellerId() {
    return sellerId;
  }

  public Category getCategory() {
    return category;
  }
}
