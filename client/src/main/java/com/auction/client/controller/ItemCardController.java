package com.auction.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import com.auction.client.service.WatchlistService;

public class ItemCardController {

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



    // Hàm này sẽ được HomeController gọi để truyền dữ liệu vào
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

        // Format số tiền cho đẹp (vd: 1000.0 -> "1,000 USD")
        priceLabel.setText(String.format("%,.0f USD", price));

        // Xử lý chữ "bid" hay "bids"
        bidsLabel.setText(bids + (bids <= 1 ? " bid" : " bids"));
        timeLabel.setText("Ending In : " + time);

        if (cardRoot != null) {
            cardRoot.setOnMouseClicked(event -> handleCardClick());
        }
        //Set màu cho status
        if (status.equals("Active")) {
            statusLabel.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 2 5 2 5;");
        }
        if (status.equals("End")) {
            statusLabel.setStyle("-fx-background-color: #FF3737; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 2 5 2 5;");
        }
        if (status.equals("In Queue")) {
            statusLabel.setStyle("-fx-background-color: #4C8CE4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 2 5 2 5;");
        }
        if (status.equals("Cacelled")) {
            statusLabel.setStyle("-fx-background-color: #605B51; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 2 5 2 5;");
        }

    }

    @FXML
    private void handleCardClick() {
        AuctionDetailController.open(icon, category, name, price, bids, time, status, auctionId);
    }
}