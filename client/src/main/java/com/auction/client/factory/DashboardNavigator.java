package com.auction.client.factory;

import java.io.IOException;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class DashboardNavigator {

  private DashboardNavigator() {}

  public static void openDashboard(Stage stage, String role) throws IOException {
    DashboardProduct dashboard = createDashboard(role);
    Scene scene = dashboard.getScene();

    stage.setScene(scene);
    stage.setTitle(dashboard.getTitle());
    stage.centerOnScreen();
    stage.show();
  }

  private static DashboardProduct createDashboard(String role) {
    RoleUIFactory factory;
    if ("Seller".equalsIgnoreCase(role)) {
      factory = new SellerUIFactory();
    } else {
      factory = new BidderUIFactory();
    }
    return factory.createDashboard();
  }
}
