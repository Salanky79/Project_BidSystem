package com.auction.client.factory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

public class SellerDashboardProduct implements DashboardProduct {
    @Override
    public Scene getScene() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/auction/client/view/SellerDashboard.fxml"));
        return new Scene(root);
    }

    @Override
    public String getTitle() {
        return "Seller Dashboard";
    }
}
