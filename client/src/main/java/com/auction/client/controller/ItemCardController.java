package com.auction.client.controller;

import com.auction.share.enums.AuctionStatus;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class ItemCardController {
    private static final String STATUS_STYLE_OPEN =
            "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 2 5 2 5;";
    private static final String STATUS_STYLE_FINISHED =
            "-fx-background-color: #FF3737; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 2 5 2 5;";
    private static final String STATUS_STYLE_RUNNING =
            "-fx-background-color: #4C8CE4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 2 5 2 5;";
    private static final String STATUS_STYLE_CANCELLED =
            "-fx-background-color: #605B51; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 2 5 2 5;";

    @FXML
    private HBox cardRoot;
    @FXML
    private Label iconLabel;
    @FXML
    private Label categoryLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Label bidsLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Label statusLabel;

    private String icon;
    private String category;
    private String name;
    private String status;
    private double price;
    private int bids;
    private String time;

    public void setData(String icon, String category, String name, double price, int bids, String time, String status) {
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

        priceLabel.setText(String.format("%,.0f USD", price));
        bidsLabel.setText(bids + (bids <= 1 ? " bid" : " bids"));
        timeLabel.setText("Ending In : " + time);

        if (cardRoot != null) {
            cardRoot.setOnMouseClicked(event -> handleCardClick());
        }

        applyStatusStyle(status);
    }

    private void applyStatusStyle(String status) {
        if (status.equalsIgnoreCase(String.valueOf(AuctionStatus.RUNNING))) {
            statusLabel.setStyle(STATUS_STYLE_RUNNING);
        } else if (status.equalsIgnoreCase(String.valueOf(AuctionStatus.FINISHED))) {
            statusLabel.setStyle(STATUS_STYLE_FINISHED);
        } else if (status.equalsIgnoreCase(String.valueOf(AuctionStatus.OPEN))) {
            statusLabel.setStyle(STATUS_STYLE_OPEN);
        } else if (status.equalsIgnoreCase(String.valueOf(AuctionStatus.CANCELED))) {
            statusLabel.setStyle(STATUS_STYLE_CANCELLED);
        }
    }



    @FXML
    private void handleCardClick() {
        AuctionDetailController.open(icon, category, name, price, bids, time, status);
    }
}
