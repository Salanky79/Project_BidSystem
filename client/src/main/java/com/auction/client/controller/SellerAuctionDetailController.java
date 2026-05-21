package com.auction.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class SellerAuctionDetailController {

    @FXML private Button closeButton;
    @FXML private Label productTitleLabel;

    private String auctionId;

    @FXML
    public void initialize() {
        if (closeButton != null) {
            closeButton.setOnAction(e -> {
                Stage stage = (Stage) closeButton.getScene().getWindow();
                stage.close();
            });
        }
    }

    public void setData(String icon, String category, String name, double price, int bids, String time, String status, String auctionId) {
        this.auctionId = auctionId;
        if (productTitleLabel != null) {
            productTitleLabel.setText(name);
        }
        // TODO: Load more specific seller details using the auctionId
    }

    public static void open(String icon, String category, String name, double price, int bids, String time, String status, String auctionId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SellerAuctionDetailController.class.getResource("/com/auction/client/view/SellerAuctionDetailView.fxml")
            );
            Parent root = loader.load();

            SellerAuctionDetailController ctrl = loader.getController();
            ctrl.setData(icon, category, name, price, bids, time, status, auctionId);

            Stage stage = new Stage();
            stage.setTitle("Seller Auction Management - " + name);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading SellerAuctionDetailView.fxml");
        }
    }
}
