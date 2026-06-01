package com.auction.client.controller;

import com.auction.client.service.AuctionService;
import com.auction.client.utils.CategoryUtils;
import com.auction.share.DTO.AuctionSummaryDTO;
import com.auction.share.enums.AuctionStatus;
import java.io.IOException;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 * Controller cho SellerDashboard.fxml. Quản lý: stat boxes, filter bar, auction card grid.
 * Controller xử lý logic giao diện bảng điều khiển chính (Dashboard) của người bán.
 */
public class SellerDashboardController {



  private AuctionService auctionService;

  public void setAuctionService(AuctionService auctionService) {
    this.auctionService = auctionService;
    loadAuctionCards("All");
  }

  // ===== FILTER BUTTONS =====
  @FXML private Button btnFilterAll;
  @FXML private Button btnFilterActive;
  @FXML private Button btnFilterEnded;
  @FXML private Button btnDateMonth;
  @FXML private Button btnDate7Days;


  // ===== CARD GRID =====
  @FXML private GridPane auctionGrid;

  @FXML
  public void initialize() {
    // Cards will be loaded dynamically when setAuctionService is called
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






  // D1: Cache dữ liệu local để chuyển tab filter mượt mà, không spam API liên tục
  private List<AuctionSummaryDTO> allAuctions = null;
  private final java.util.Map<String, javafx.scene.Node> cardCache = new java.util.HashMap<>();

  private String currentFilterStatus = "All";

  private String getCurrentFilterStatus() {
      return currentFilterStatus;
  }

  // ===== LOAD CARDS =====

  private void loadAuctionCards(String filterStatus) {
    this.currentFilterStatus = filterStatus;
    if (allAuctions != null) {
      renderGrid(filterStatus);
      return;
    }

    if (auctionService == null) return;
    auctionService
        .getSellerAuctions(
            null,
            response -> {
              Platform.runLater(
                  () -> {
                    if (response != null
                        && response.isSuccess()
                        && response.getData() instanceof List<?> list) {
                      allAuctions = new java.util.ArrayList<>();
                      for (Object obj : list) {
                        if (obj instanceof AuctionSummaryDTO dto) {
                          allAuctions.add(dto);
                        }
                      }
                      renderGrid(filterStatus);
                    } else {
                      System.out.println("Failed to load seller auctions.");
                    }
                  });
            });
  }

  private void renderGrid(String filterStatus) {
    auctionGrid.getChildren().clear();
    if (allAuctions == null) return;

    int col = 0, row = 0;
    for (AuctionSummaryDTO dto : allAuctions) {
      if (!matchesFilter(filterStatus, dto.getStatus())) continue;

      try {
        javafx.scene.Node card = cardCache.get(dto.getAuctionId());
        if (card == null) {
          FXMLLoader loader =
              new FXMLLoader(
                  getClass()
                      .getResource(
                          "/com/auction/client/view/SellerItemCard.fxml"));
          card = loader.load();
          SellerItemCardController ctrl = loader.getController();

          ctrl.setRefreshCallback(() -> {
            allAuctions = null;
            cardCache.clear();
            loadAuctionCards(getCurrentFilterStatus());
          });

          ctrl.setData(
              CategoryUtils.iconFor(dto.getCategory()),
              dto.getCategory(),
              dto.getItemName(),
              dto.getCurrentPrice(),
              0, // bids
              dto.getEndTime(),
              dto.getStatus(),
              dto.getAuctionId(),
              dto.getImageUrl());
          cardCache.put(dto.getAuctionId(), card);
        }

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

  // B3: Dùng AuctionStatus enum thay vì so sánh magic string trực tiếp
  private boolean matchesFilter(String filter, String statusRaw) {
    if ("All".equalsIgnoreCase(filter)) return true;
    AuctionStatus status = com.auction.share.enums.AuctionStatus.from(statusRaw);
    return switch (filter) {
      case "Active" -> status.isActive();
      case "Ended"  -> status == com.auction.share.enums.AuctionStatus.FINISHED;
      default       -> status.getDisplayName().equalsIgnoreCase(filter);
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
