package com.auction.client.factory;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class BidderDashboard implements DashboardProduct {
  @Override
  public Scene getScene() throws IOException {
    FXMLLoader loader =
        new FXMLLoader(getClass().getResource("/com/auction/client/view/HomeFrame.fxml"));
    Parent root = loader.load();

    com.auction.client.controller.HomeFrameController controller = loader.getController();
    if (controller != null) {
      controller.handleHome(); // Load Home.fxml inside HomeFrame and fetch auctions
    }
    Scene scene = new Scene(root);
    if (controller != null) {
      scene.windowProperty().addListener((obs, oldWin, newWin) -> {
        if (newWin instanceof javafx.stage.Stage stage) {
          stage.setOnCloseRequest(e -> controller.cleanup());
        }
      });
    }
    return scene;
  }

  @Override
  public String getTitle() {
    return "Home - Bidder";
  }
}
