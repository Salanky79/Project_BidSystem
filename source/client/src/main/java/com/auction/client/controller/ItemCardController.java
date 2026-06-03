package com.auction.client.controller;


import com.auction.client.factory.AppNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.auction.client.utils.CardImageLoader;
import com.auction.client.utils.DateTimeUtils;
import com.auction.share.enums.AuctionStatus;

public class ItemCardController {

  @FXML private HBox cardRoot;
  @FXML private StackPane itemcard;
  @FXML private Label iconLabel;
  @FXML private Label categoryLabel;
  @FXML private Label nameLabel;
  @FXML private Label priceLabel;
  @FXML private Label bidsLabel;
  @FXML private Label timeLabel;
  @FXML private Label statusLabel;

  private String icon;
  private String category;
  private String name;
  private String status;
  private double price;
  private double bidStep;
  private int bids;
  private String time;
  private String auctionId;



  public void setData(
      String icon,
      String category,
      String name,
      double price,
      double bidStep,
      int bids,
      String time,
      String status,
      String auctionId,
      String imageUrl,
      String highestBidderName) {
    categoryLabel.setText(category);
    nameLabel.setText(name);

    this.icon = icon;
    this.category = category;
    this.name = name;
    this.price = price;
    this.bidStep = bidStep;
    this.bids = bids;
    this.time = time;
    this.status = status;
    this.auctionId = auctionId;

    // Load ảnh sản phẩm từ Cloudinary (có error handler fallback)
    CardImageLoader.load(itemcard, iconLabel, imageUrl, icon, 160, 24);

    // Format số tiền
    priceLabel.setText(String.format("%,.0f VND", price));

    // Hiển thị số lượt bid – lấy thẳng từ DTO, không gọi thêm network
    bidsLabel.setText(bids + (bids <= 1 ? " bid" : " bids"));

    // Hiển thị thời gian kết thúc hoặc người thắng cuộc nếu đã kết thúc
    if ("FINISHED".equalsIgnoreCase(status) || "End".equalsIgnoreCase(status)) {
      if (highestBidderName != null && !highestBidderName.trim().isEmpty()) {
        timeLabel.setText("Winner: " + highestBidderName);
      } else {
        timeLabel.setText("Winner: None (No bids)");
      }
    } else {
      timeLabel.setText("Ending In : " + DateTimeUtils.formatDateTimeForDisplay(time));
    }

    if (cardRoot != null) {
      cardRoot.setOnMouseClicked(event -> handleCardClick());
    }

    // Lấy display name và style từ AuctionStatus enum
    AuctionStatus auctionStatus = AuctionStatus.from(status);
    statusLabel.setText(auctionStatus.getDisplayName());
    statusLabel.setStyle(auctionStatus.getBadgeStyle());
  }

  public void updateDynamicData(double price, int bids, String status, String time, String highestBidderName) {
    this.price = price;
    this.bids = bids;
    this.status = status;
    this.time = time;
    
    // Format số tiền
    priceLabel.setText(String.format("%,.0f VND", price));

    // Hiển thị số lượt bid
    bidsLabel.setText(bids + (bids <= 1 ? " bid" : " bids"));

    // Hiển thị thời gian kết thúc hoặc người thắng cuộc nếu đã kết thúc
    if ("FINISHED".equalsIgnoreCase(status) || "End".equalsIgnoreCase(status)) {
      if (highestBidderName != null && !highestBidderName.trim().isEmpty()) {
        timeLabel.setText("Winner: " + highestBidderName);
      } else {
        timeLabel.setText("Winner: None (No bids)");
      }
    } else {
      timeLabel.setText("Ending In : " + DateTimeUtils.formatDateTimeForDisplay(time));
    }

    AuctionStatus auctionStatus = AuctionStatus.from(status);
    statusLabel.setText(auctionStatus.getDisplayName());
    statusLabel.setStyle(auctionStatus.getBadgeStyle());
  }

  @FXML
  private void handleCardClick() {
    AppNavigator.openBidderDetail(
        icon, category, name, price, bidStep, bids, time, status, auctionId);
  }
}
