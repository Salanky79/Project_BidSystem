package com.auction.client.factory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

public class BidderDashboard implements DashboardProduct {
    @Override
    public Scene getScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/HomeFrame.fxml"));
        Parent root = loader.load();
        
        com.auction.client.controller.HomeFrameController controller = loader.getController();
        if (controller != null) {
            controller.handleHome(null); // Load Home.fxml inside HomeFrame and fetch auctions
        }
        
        return new Scene(root);
    }

    @Override
    public String getTitle() {
        return "Home - Bidder";
    }
}
