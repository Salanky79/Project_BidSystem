package com.auction.client.factory;

import com.auction.client.ClientContext;
import com.auction.client.controller.AuctionDetailController;
import com.auction.client.controller.LoginController;
import com.auction.client.controller.SellerAuctionDetailController;
import com.auction.client.controller.SignupController;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class DashboardNavigator {

  private static final Map<String, Stage> bidderStages = Collections.synchronizedMap(new WeakHashMap<>());
  private static final Map<String, Stage> sellerStages = Collections.synchronizedMap(new WeakHashMap<>());

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

  public static void openBidderDetail(
      String icon,
      String category,
      String name,
      double price,
      double bidStep,
      int bids,
      String time,
      String status,
      String auctionId
  ) {
    Stage existingStage = bidderStages.get(auctionId);
    if (existingStage != null && existingStage.isShowing()) {
      existingStage.requestFocus();
      return;
    }

    try {
      FXMLLoader loader = new FXMLLoader(
          DashboardNavigator.class.getResource("/com/auction/client/view/AuctionDetailv2.fxml")
      );
      Parent root = loader.load();

      AuctionDetailController ctrl = loader.getController();
      ctrl.setServices(ClientContext.bidService(), ClientContext.auctionService(), ClientContext.socketClient());
      ctrl.setData(icon, category, name, price, bidStep, bids, time, status, auctionId);

      Stage stage = new Stage();
      stage.setTitle("Auction – " + name);
      stage.setScene(new Scene(root));

      bidderStages.put(auctionId, stage);

      stage.setOnCloseRequest(event -> {
        ctrl.cleanup();
        bidderStages.remove(auctionId);
      });
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("Error loading AuctionDetailv2.fxml");
    }
  }

  public static void openSellerDetail(
      String icon,
      String category,
      String name,
      double price,
      int bids,
      String time,
      String status,
      String auctionId,
      Runnable dashboardInvalidator
  ) {
    Stage existingStage = sellerStages.get(auctionId);
    if (existingStage != null && existingStage.isShowing()) {
      existingStage.requestFocus();
      return;
    }

    try {
      FXMLLoader loader = new FXMLLoader(
          DashboardNavigator.class.getResource("/com/auction/client/view/SellerAuctionDetailView.fxml")
      );
      Parent root = loader.load();

      SellerAuctionDetailController ctrl = loader.getController();
      ctrl.setDashboardInvalidator(dashboardInvalidator);
      ctrl.setServices(ClientContext.auctionService(), ClientContext.socketClient());
      ctrl.setData(icon, category, name, price, bids, time, status, auctionId);

      Stage stage = new Stage();
      stage.setTitle("Seller Auction Management - " + name);
      stage.setScene(new Scene(root));

      sellerStages.put(auctionId, stage);

      stage.setOnCloseRequest(event -> {
        ctrl.cleanup();
        sellerStages.remove(auctionId);
      });
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("Error loading SellerAuctionDetailView.fxml");
    }
  }

  public static void showLogin(Stage stage) {
    try {
      FXMLLoader loader = new FXMLLoader(
          DashboardNavigator.class.getResource("/com/auction/client/view/Login.fxml")
      );
      Parent root = loader.load();

      if (loader.getController() instanceof LoginController loginCtrl) {
        loginCtrl.setUserService(ClientContext.userService());
      }

      stage.setScene(new Scene(root));
      stage.centerOnScreen();
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("Error loading Login.fxml");
    }
  }

  public static void showSignup(Stage stage) {
    try {
      FXMLLoader loader = new FXMLLoader(
          DashboardNavigator.class.getResource("/com/auction/client/view/SignupView.fxml")
      );
      Parent root = loader.load();

      if (loader.getController() instanceof SignupController signupCtrl) {
        signupCtrl.setUserService(ClientContext.userService());
      }

      stage.setScene(new Scene(root));
      stage.centerOnScreen();
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("Error loading SignupView.fxml");
    }
  }
}
