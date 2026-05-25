package com.auction.client.controller;

import com.auction.client.service.DeletedAuctionsStore;

import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class SellerItemCardController {

  @FXML private HBox cardRoot;
  @FXML private Label iconLabel;
  @FXML private Label categoryLabel;
  @FXML private Label nameLabel;
  @FXML private Label priceLabel;
  @FXML private Label bidsLabel;
  @FXML private Label timeLabel;
  @FXML private Label statusLabel;
  @FXML private Button deleteButton;

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
    statusLabel.setText(status);
    iconLabel.setText(icon);
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



    priceLabel.setText(String.format("%,.0f VND", price));

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

    // ── Delete button: only visible for CANCELED auctions ──
    if (deleteButton != null) {
      if ("CANCELED".equals(status)) {
        deleteButton.setVisible(true);
        deleteButton.setManaged(true);
        deleteButton.setOnAction(
            event -> {
              event.consume(); // prevent click from bubbling to cardRoot
              handleDelete();
            });
      } else {
        deleteButton.setVisible(false);
        deleteButton.setManaged(false);
      }
    }

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
    if (status.equals("CANCELED")) { // Keep matching original logic
      statusLabel.setStyle(
          "-fx-background-color: #605B51; -fx-text-fill: white; -fx-font-weight: bold;"
              + " -fx-background-radius: 5px; -fx-padding: 2 5 2 5;");
    }
  }

  private void handleDelete() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Delete Auction");
    alert.setHeaderText("Delete \"" + name + "\"?");
    alert.setContentText("This auction will be removed from the listing. This action cannot be undone during this session.");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      DeletedAuctionsStore.getInstance().delete(auctionId);
      if (refreshCallback != null) {
        refreshCallback.run();
      }
    }
  }

  @FXML
  private void handleCardClick() {
    SellerAuctionDetailController.open(icon, category, name, price, bids, time, status, auctionId);
  }
}

