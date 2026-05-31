package com.auction.client.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.auction.client.utils.CardImageLoader;
import com.auction.client.utils.DateTimeUtils;
import com.auction.share.enums.AuctionStatus;

public class SellerItemCardController {

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
  private int bids;
  private String time;
  private String auctionId;



  private Runnable refreshCallback;

  public void setRefreshCallback(Runnable refreshCallback) {
    this.refreshCallback = refreshCallback;
  }

  public void setData(
      String icon,
      String category,
      String name,
      double price,
      int bids,
      String time,
      String status,
      String auctionId) {
    setData(icon, category, name, price, bids, time, status, auctionId, null);
  }

  public void setData(
      String icon,
      String category,
      String name,
      double price,
      int bids,
      String time,
      String status,
      String auctionId,
      String imageUrl) {
    statusLabel.setText(status);
    categoryLabel.setText(category);
    nameLabel.setText(name);

    this.icon = icon;
    this.category = category;
    this.name = name;
    this.price = price;
    this.bids = bids;
    this.time = time;
    this.status = status;
    this.auctionId = auctionId;

    // Load ảnh sản phẩm từ Cloudinary (có error handler fallback)
    CardImageLoader.load(itemcard, iconLabel, imageUrl, icon, 160, 24);

    priceLabel.setText(String.format("%,.0f VND", price));

    bidsLabel.setText(bids + (bids <= 1 ? " bid" : " bids"));
    timeLabel.setText("Ending In : " + DateTimeUtils.formatDateTimeForDisplay(time));

    // A3: bidCount đã được server tính sẵn trong AuctionSummaryDTO.getBidCount()
    // Không cần fetch lại getAuctionDetail() — tránh N+1 request

    if (cardRoot != null) {
      cardRoot.setOnMouseClicked(event -> handleCardClick());
    }

    // B2: Dùng AuctionStatus enum thay vì magic string
    AuctionStatus auctionStatus = AuctionStatus.from(status);
    statusLabel.setText(auctionStatus.getDisplayName());
    statusLabel.setStyle(auctionStatus.getBadgeStyle());
  }



  @FXML
  private void handleCardClick() {
    SellerAuctionDetailController.open(icon, category, name, price, bids, time, status, auctionId, refreshCallback);
  }
}
