package com.auction.client.factory;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class SellerDashboardProduct implements DashboardProduct {
  @Override
  public Scene getScene() throws IOException {
    Parent root =
        FXMLLoader.load(
            getClass().getResource("/com/auction/client/view/SellerDashboardFrame.fxml"));
    return new Scene(root);
  }

  @Override
  public String getTitle() {
    return "Seller Dashboard";
  }
}
