package com.auction.client.controller;


import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class ItemCardController {

  @FXML private HBox cardRoot;
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

  // Hàm này sẽ được HomeController gọi để truyền dữ liệu vào
  public void setData(
      String icon,
      String category,
      String name,
      double price,
      double bidStep,
      int bids,
      String time,
      String status,
      String auctionId) {
    statusLabel.setText(status);
    iconLabel.setText(icon);
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



    // Format số tiền cho đẹp (vd: 1000.0 -> "1,000 USD")
    priceLabel.setText(String.format("%,.0f VND", price));

    // Xử lý chữ "bid" hay "bids"
    bidsLabel.setText(bids + (bids <= 1 ? " bid" : " bids"));
    timeLabel.setText("Ending In : " + time);

    // Fetch actual bid count asynchronously
    com.auction.client.ClientContext.auctionService().getAuctionDetail(this.auctionId, response -> {
      if (response != null && response.isSuccess() && response.getData() instanceof com.auction.share.DTO.AuctionDetailDTO detail) {
        int actualBids = detail.getBidHistory() != null ? detail.getBidHistory().size() : 0;
        this.bids = actualBids;
        javafx.application.Platform.runLater(() -> {
          if (bidsLabel != null) {
            bidsLabel.setText(actualBids + (actualBids <= 1 ? " bid" : " bids"));
          }
        });
      }
    });

    if (cardRoot != null) {
      cardRoot.setOnMouseClicked(event -> handleCardClick());
    }
    // Set màu cho status
    if (status.equals("Active")) {
      statusLabel.setStyle(
          "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;"
              + " -fx-background-radius: 5px; -fx-padding: 2 5 2 5;");
    }
    if (status.equals("End")) {
      statusLabel.setStyle(
          "-fx-background-color: #FF3737; -fx-text-fill: white; -fx-font-weight: bold;"
              + " -fx-background-radius: 5px; -fx-padding: 2 5 2 5;");
    }
    if (status.equals("In Queue")) {
      statusLabel.setStyle(
          "-fx-background-color: #4C8CE4; -fx-text-fill: white; -fx-font-weight: bold;"
              + " -fx-background-radius: 5px; -fx-padding: 2 5 2 5;");
    }
    if (status.equals("CANCELED")) {
      statusLabel.setStyle(
          "-fx-background-color: #605B51; -fx-text-fill: white; -fx-font-weight: bold;"
              + " -fx-background-radius: 5px; -fx-padding: 2 5 2 5;");
    }
  }

  @FXML
  private void handleCardClick() {
    AuctionDetailController.open(
        icon, category, name, price, bidStep, bids, time, status, auctionId);
  }
}
