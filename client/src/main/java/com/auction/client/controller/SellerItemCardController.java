package com.auction.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import com.auction.client.service.WatchlistService;

public class SellerItemCardController {

    @FXML private HBox cardRoot;
    @FXML private Label iconLabel;
    @FXML private Label categoryLabel;
    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label bidsLabel;
    @FXML private Label timeLabel;
    @FXML private Label statusLabel;
    @FXML private Label followStatusLabel;

    private String icon;
    private String category;
    private String name;
    private String status;
    private double price;
    private int bids;
    private String time;
    private String auctionId;

    public void setData(String icon, String category, String name, double price, int bids, String time, String status, String auctionId) {
        statusLabel.setText(status);
        iconLabel.setText(icon);
        categoryLabel.setText(category);
        nameLabel.setText(name);

        this.icon     = icon;
        this.category = category;
        this.name     = name;
        this.price    = price;
        this.bids     = bids;
        this.time = time;
        this.status = status;
        this.auctionId = auctionId;
        
        if (followStatusLabel != null) {
            boolean isFollowed = WatchlistService.getInstance().isFollowed(auctionId);
            followStatusLabel.setVisible(isFollowed);
            followStatusLabel.setManaged(isFollowed);
        }

        priceLabel.setText(String.format("%,.0f USD", price));

        bidsLabel.setText(bids + (bids <= 1 ? " bid" : " bids"));
        timeLabel.setText("Ending In : " + time);

        if (cardRoot != null) {
            cardRoot.setOnMouseClicked(event -> handleCardClick());
        }

        if (status.equals("Active")) {
            statusLabel.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 2 5 2 5;");
        }
        if (status.equals("End")) {
            statusLabel.setStyle("-fx-background-color: #FF3737; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 2 5 2 5;");
        }
        if (status.equals("In Queue")) {
            statusLabel.setStyle("-fx-background-color: #4C8CE4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 2 5 2 5;");
        }
        if (status.equals("CANCELED")) { // Keep matching original logic
            statusLabel.setStyle("-fx-background-color: #605B51; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 2 5 2 5;");
        }
    }

    @FXML
    private void handleCardClick() {
        SellerAuctionDetailController.open(icon, category, name, price, bids, time, status, auctionId);
    }
}
