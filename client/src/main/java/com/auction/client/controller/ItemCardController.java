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

public class ItemCardController {

    @FXML private HBox cardRoot;
    @FXML private Label iconLabel;
    @FXML private Label categoryLabel;
    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label bidsLabel;
    @FXML private Label timeLabel;

    private String icon;
    private String category;
    private String name;
    private double price;
    private int bids;
    private String time;


    // Hàm này sẽ được HomeController gọi để truyền dữ liệu vào
    public void setData(String icon, String category, String name, double price, int bids, String time) {


        iconLabel.setText(icon);
        categoryLabel.setText(category);
        nameLabel.setText(name);

        this.icon     = icon;
        this.category = category;
        this.name     = name;
        this.price    = price;
        this.bids     = bids;
        this.time = time;

        // Format số tiền cho đẹp (vd: 1000.0 -> "1,000 USD")
        priceLabel.setText(String.format("%,.0f USD", price));

        // Xử lý chữ "bid" hay "bids"
        bidsLabel.setText(bids + (bids <= 1 ? " bid" : " bids"));
        timeLabel.setText("Ending In : " + time);

        if (cardRoot != null) {
            cardRoot.setOnMouseClicked(event -> handleCardClick());
        }

    }

    @FXML
    private void handleCardClick() {
        AuctionDetailController.open(icon, category, name, price, bids, time);
    }
}