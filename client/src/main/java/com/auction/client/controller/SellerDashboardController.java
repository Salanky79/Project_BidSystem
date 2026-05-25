package com.auction.client.controller;

import com.auction.client.ClientContext;
import com.auction.share.DTO.AuctionSummaryDTO;
import java.io.IOException;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 * Controller cho SellerDashboard.fxml. Quản lý: stat boxes, filter bar, auction card grid.
 * Controller xử lý logic giao diện bảng điều khiển chính (Dashboard) của người bán.
 */
public class SellerDashboardController {

  // ===== STAT LABELS =====
  @FXML private Label labelRevenue;
  @FXML private Label labelActive;
  @FXML private Label labelCompleted;
  @FXML private Label labelShipment;

  // ===== FILTER BUTTONS =====
  @FXML private Button btnFilterAll;
  @FXML private Button btnFilterActive;
  @FXML private Button btnFilterEnded;
  @FXML private Button btnDateMonth;
  @FXML private Button btnDate7Days;
  @FXML private Button btnManage;

  // ===== CARD GRID =====
  @FXML private GridPane auctionGrid;

  @FXML
  public void initialize() {
    // Load auction cards vào grid
    loadAuctionCards("All");
  }

  // ===== FILTER HANDLERS =====

  @FXML
  public void handleFilterAll() {
    setStatusFilter("All");
    loadAuctionCards("All");
  }

  @FXML
  public void handleFilterActive() {
    setStatusFilter("Active");
    loadAuctionCards("Active");
  }

  @FXML
  public void handleFilterEnded() {
    setStatusFilter("Ended");
    loadAuctionCards("Ended");
  }

  @FXML
  public void handleDateMonth() {
    setDateFilter("Month");
  }

  @FXML
  public void handleDate7Days() {
    setDateFilter("7Days");
  }

  @FXML
  public void handleManage() {
    System.out.println("Open manage panel.");
  }

  // ===== LOAD CARDS =====

  private void loadAuctionCards(String filterStatus) {
    String sellerId = ClientContext.userService().getSessionManager().getCurrentUserId();
    if (sellerId == null) {
      System.out.println("User not logged in, cannot load seller auctions.");
      return;
    }

    ClientContext.auctionService()
        .getSellerAuctions(
            sellerId,
            null,
            response -> {
              Platform.runLater(
                  () -> {
                    auctionGrid.getChildren().clear();
                    if (response != null
                        && response.isSuccess()
                        && response.getData() instanceof List<?> list) {
                      int col = 0, row = 0;
                      for (Object obj : list) {
                        if (obj instanceof AuctionSummaryDTO dto) {
                          if (!matchesFilter(filterStatus, dto.getStatus())) continue;
                          try {
                            FXMLLoader loader =
                                new FXMLLoader(
                                    getClass()
                                        .getResource(
                                            "/com/auction/client/view/SellerItemCard.fxml"));
                            HBox card = loader.load();
                            SellerItemCardController ctrl = loader.getController();

                            ctrl.setData(
                                iconForCategory(dto.getCategory()),
                                dto.getCategory(),
                                dto.getItemName(),
                                dto.getCurrentPrice(),
                                0, // bids
                                dto.getEndTime(),
                                dto.getStatus(),
                                dto.getAuctionId());

                            auctionGrid.add(card, col++, row);
                            GridPane.setMargin(card, new Insets(10));
                            if (col == 2) {
                              col = 0;
                              row++;
                            }
                          } catch (IOException e) {
                            e.printStackTrace();
                          }
                        }
                      }
                    } else {
                      System.out.println("Failed to load seller auctions.");
                    }
                  });
            });
  }

  private boolean matchesFilter(String filter, String status) {
    if ("Active".equalsIgnoreCase(filter)
        && ("Active".equalsIgnoreCase(status)
            || "RUNNING".equalsIgnoreCase(status)
            || "OPEN".equalsIgnoreCase(status)
            || "In Queue".equalsIgnoreCase(status))) {
      return true;
    }
    if ("Ended".equalsIgnoreCase(filter)
        && ("End".equalsIgnoreCase(status) || "FINISHED".equalsIgnoreCase(status))) {
      return true;
    }
    if ("Draft".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
      return false;
    }
    return "All".equalsIgnoreCase(filter) || status.equalsIgnoreCase(filter);
  }

  private String iconForCategory(String category) {
    if (category == null) return "📦";
    return switch (category) {
      case "Electronic" -> "📱";
      case "Watch" -> "⌚";
      case "Hand Bag", "Clothing" -> "👜";
      case "Car" -> "🚗";
      case "Art" -> "🖼";
      case "Jewelry" -> "💍";
      default -> "📦";
    };
  }

  // ===== STYLE HELPERS =====

  private static final String STYLE_ACTIVE =
      "-fx-background-color: #1a1a1a; -fx-text-fill: white; "
          + "-fx-font-size: 12px; -fx-border-radius: 6; -fx-background-radius: 6; "
          + "-fx-padding: 4 12 4 12; -fx-cursor: hand;";

  private static final String STYLE_INACTIVE =
      "-fx-background-color: transparent; -fx-text-fill: #555555; "
          + "-fx-font-size: 12px; -fx-border-radius: 6; -fx-background-radius: 6; "
          + "-fx-padding: 4 12 4 12; -fx-cursor: hand; "
          + "-fx-border-color: #cccccc; -fx-border-width: 1;";

  private void setStatusFilter(String selected) {
    btnFilterAll.setStyle("All".equals(selected) ? STYLE_ACTIVE : STYLE_INACTIVE);
    btnFilterActive.setStyle("Active".equals(selected) ? STYLE_ACTIVE : STYLE_INACTIVE);
    btnFilterEnded.setStyle("Ended".equals(selected) ? STYLE_ACTIVE : STYLE_INACTIVE);
  }

  private void setDateFilter(String selected) {
    btnDateMonth.setStyle("Month".equals(selected) ? STYLE_ACTIVE : STYLE_INACTIVE);
    btnDate7Days.setStyle("7Days".equals(selected) ? STYLE_ACTIVE : STYLE_INACTIVE);
  }
}
