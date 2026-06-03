package com.auction.client.controller;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;
import javafx.application.Platform;
import com.auction.client.ClientContext;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.DTO.Response;

public class SellerDashboardFrameController extends FrameController {

  private SellerDashboardController currentDashboardController;
  private SellerListController currentListController;
  private Consumer<Response<?>> bidPushListener;

  @FXML private Label usernameLabel;

  // Sidebar buttons – cần fx:id để highlight
  @FXML private Button btnHome;
  @FXML private Button btnActive;
  @FXML private Button btnSold;
  @FXML private Button btnProfile;

  // ── Styles ──────────────────────────────────────────────────────────────
  private static final String STYLE_SIDEBAR_ACTIVE =
      "-fx-background-color: #f0a500; -fx-text-fill: white; "
          + "-fx-font-size: 15px; -fx-font-weight: bold; "
          + "-fx-border-radius: 8; -fx-background-radius: 8; "
          + "-fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 20; -fx-cursor: hand;";

  private static final String STYLE_SIDEBAR_INACTIVE =
      "-fx-background-color: transparent; -fx-text-fill: white; "
          + "-fx-font-size: 15px; "
          + "-fx-border-radius: 8; -fx-background-radius: 8; "
          + "-fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 20; -fx-cursor: hand;";

  private static final String STYLE_SIDEBAR_INACTIVE_INDENT =
      "-fx-background-color: transparent; -fx-text-fill: #c9c9c9; "
          + "-fx-font-size: 15px; "
          + "-fx-border-radius: 8; -fx-background-radius: 8; "
          + "-fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 40; -fx-cursor: hand;";

  private static final String STYLE_SIDEBAR_ACTIVE_INDENT =
      "-fx-background-color: #f0a500; -fx-text-fill: white; "
          + "-fx-font-size: 15px; -fx-font-weight: bold; "
          + "-fx-border-radius: 8; -fx-background-radius: 8; "
          + "-fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 40; -fx-cursor: hand;";

  @FXML
  public void initialize() {
    setupPushListener();
    changeView("/com/auction/client/view/SellerDashboard.fxml", controller -> {
      if (controller instanceof SellerDashboardController sdc) {
        currentDashboardController = sdc;
        currentListController = null;
        sdc.setAuctionService(com.auction.client.ClientContext.auctionService());
      }
    });
    highlightButton("Home");
  }

  private void setupPushListener() {
      bidPushListener = response -> {
          if (response != null && response.isSuccess()) {
              if (response.getData() instanceof BidUpdateEvent event
                      && "BID_UPDATED".equals(response.getMessage())) {
                  if (currentDashboardController != null) {
                      currentDashboardController.updateCardOnBidEvent(event);
                  }
                  if (currentListController != null) {
                      currentListController.updateCardOnBidEvent(event);
                  }
              } else if (("AUCTION_FINISHED".equals(response.getMessage()) || "AUCTION_CANCELLED".equals(response.getMessage()))
                      && response.getData() instanceof String auctionId) {
                  javafx.application.Platform.runLater(() -> {
                      if (currentDashboardController != null) {
                          currentDashboardController.loadAuctionCards(currentDashboardController.getCurrentFilterStatus());
                      }
                      if (currentListController != null) {
                          currentListController.loadItems(currentListController.getCurrentMode());
                      }
                  });
              }
          }
      };
      ClientContext.socketClient().addPushListener(bidPushListener);
  }

  public void cleanup() {
      if (bidPushListener != null) {
          ClientContext.socketClient().removePushListener(bidPushListener);
          bidPushListener = null;
      }
  }

  // ===== TOPBAR =====

  @FXML
  public void handleLogout(ActionEvent event) {
    cleanup();
    showLogin(event);
  }

  // ===== SIDEBAR NAVIGATION =====

  @FXML
  public void handleHome() {
    changeView("/com/auction/client/view/SellerDashboard.fxml", controller -> {
      if (controller instanceof SellerDashboardController sdc) {
        currentDashboardController = sdc;
        currentListController = null;
        sdc.setAuctionService(com.auction.client.ClientContext.auctionService());
      }
    });
    highlightButton("Home");
  }

  @FXML
  public void handleActive() {
    loadSellerList("Active");
    highlightButton("Active");
  }

  @FXML
  public void handleSold() {
    loadSellerList("Sold");
    highlightButton("Sold");
  }

  @FXML
  public void handleProfile() {
    changeView("/com/auction/client/view/profile.fxml", obj -> {
      if (obj instanceof ProfileController profileCtrl) {
        profileCtrl.setUserService(com.auction.client.ClientContext.userService());
      }
    });
    highlightButton("Profile");
  }

  @FXML
  public void handleSell() {
    changeView("/com/auction/client/view/Sell.fxml", controller -> {
      if (controller instanceof SellController sellController) {
        sellController.setAuctionService(com.auction.client.ClientContext.auctionService());
        sellController.setOnSuccessCallback(() -> {
          changeView("/com/auction/client/view/SellerDashboard.fxml", sdCtrl -> {
            if (sdCtrl instanceof SellerDashboardController sdc) {
              sdc.setAuctionService(com.auction.client.ClientContext.auctionService());
            }
          });
          highlightButton("Home");
        });
      }
    });
  }

  // ===== HELPERS =====

  /**
   * Load SellerListView.fxml vào scrollContent rồi gọi loadItems(mode) để fetch đúng tab (Active /
   * Sold).
   */
  private void loadSellerList(String mode) {
    try {
      FXMLLoader loader =
          new FXMLLoader(getClass().getResource("/com/auction/client/view/SellerListView.fxml"));
      VBox view = loader.load();
      scrollContent.setContent(view);

      SellerListController ctrl = loader.getController();
      currentListController = ctrl;
      currentDashboardController = null;
      ctrl.setAuctionService(com.auction.client.ClientContext.auctionService());
      ctrl.loadItems(mode);
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("[SellerDashboardFrame] Cannot load SellerListView.fxml");
    }
  }

  /** Highlight đúng button trên sidebar tương ứng với màn hình đang hiển thị. */
  private void highlightButton(String active) {
    // Top-level buttons (không indent)
    if (btnHome != null) {
      btnHome.setStyle("Home".equals(active) ? STYLE_SIDEBAR_ACTIVE : STYLE_SIDEBAR_INACTIVE);
    }
    if (btnProfile != null) {
      btnProfile.setStyle("Profile".equals(active) ? STYLE_SIDEBAR_ACTIVE : STYLE_SIDEBAR_INACTIVE);
    }

    // Indented sub-buttons
    if (btnActive != null) {
      btnActive.setStyle(
          "Active".equals(active) ? STYLE_SIDEBAR_ACTIVE_INDENT : STYLE_SIDEBAR_INACTIVE_INDENT);
    }
    if (btnSold != null) {
      btnSold.setStyle(
          "Sold".equals(active) ? STYLE_SIDEBAR_ACTIVE_INDENT : STYLE_SIDEBAR_INACTIVE_INDENT);
    }
  }
}
