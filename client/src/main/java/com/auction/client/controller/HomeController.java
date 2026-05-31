package com.auction.client.controller;

import com.auction.client.ClientContext;
import com.auction.client.service.AuctionService;

import com.auction.client.utils.CategoryUtils;
import com.auction.share.DTO.AuctionSummaryDTO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class HomeController {

  private AuctionService auctionService;

  public void setAuctionService(AuctionService auctionService) {
    this.auctionService = auctionService;
  }

  // ── FXML bindings ──────────────────────────────────────────────────────────
  @FXML private GridPane auctionGrid;

  // Category filter buttons
  @FXML private Button btnAll;
  @FXML private Button btnAntique;
  @FXML private Button btnArt;
  @FXML private Button btnElectronic;
  @FXML private Button btnJewelry;
  @FXML private Button btnRealEstate;
  @FXML private Button btnVehicle;

  // ── State ──────────────────────────────────────────────────────────────────
  /** Cached list of all auctions fetched from server (avoids repeated network calls). */
  private final List<AuctionSummaryDTO> allAuctions = new ArrayList<>();

  /** Current status filter set by sidebar (e.g. "All", "Active"). */
  private String currentStatusFilter = "All";

  /** Current category filter set by the filter bar (e.g. "All", "Jewelry", "Cars"). */
  private String currentCategoryFilter = "All";

  /** Cache các HBox card node theo auctionId – tránh load lại FXML mỗi lần filter. */
  private final Map<String, HBox> cardCache = new HashMap<>();

  // ── Button style constants ─────────────────────────────────────────────────
  private static final String STYLE_ACTIVE =
      "-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-font-size: 13px; "
          + "-fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;";
  private static final String STYLE_INACTIVE =
      "-fx-background-color: #e0e0e0; -fx-text-fill: #333333; -fx-font-size: 13px; "
          + "-fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;";

  // ── Initializer ───────────────────────────────────────────────────────────
  @FXML
  public void initialize() {
    // "All" is active by default (matches the initial FXML dark style)
    updateFilterButtons("All");
  }

  // ── Public API called by HomeFrameController ───────────────────────────────
  /**
   * Fetches auctions from server filtered by status, then renders the grid. Resets category filter
   * to "All" whenever the sidebar navigation changes.
   */
  public void loadAuction(String filterStatus) {
    this.currentStatusFilter = filterStatus;
    this.currentCategoryFilter = "All";
    updateFilterButtons("All");

    allAuctions.clear();
    cardCache.clear(); // reset cache khi load dữ liệu mới từ server
    auctionGrid.getChildren().clear();

    auctionService.getAuctions(
        response ->
            Platform.runLater(
                () -> {
                  if (response != null
                      && response.isSuccess()
                      && response.getData() instanceof List<?> list) {
                    for (Object obj : list) {
                      if (obj instanceof AuctionSummaryDTO dto) {
                        allAuctions.add(dto);
                      }
                    }
                    renderGrid();
                  } else {
                    System.out.println(
                        "Failed to load auctions: "
                            + (response != null ? response.getMessage() : "Unknown error"));
                  }
                }));
  }

  // ── Category filter handlers ───────────────────────────────────────────────
  @FXML
  private void handleFilterAll() {
    applyCategory("All");
  }

  @FXML
  private void handleFilterAntique() {
    applyCategory("Antique");
  }

  @FXML
  private void handleFilterArt() {
    applyCategory("Art");
  }

  @FXML
  private void handleFilterElectronic() {
    applyCategory("Electronic");
  }

  @FXML
  private void handleFilterJewelry() {
    applyCategory("Jewelry");
  }

  @FXML
  private void handleFilterRealEstate() {
    applyCategory("RealEstate");
  }

  @FXML
  private void handleFilterVehicle() {
    applyCategory("Vehicle");
  }

  private void applyCategory(String category) {
    currentCategoryFilter = category;
    updateFilterButtons(category);
    renderGrid(); // Re-render from cache – no extra network call
  }

  // ── Rendering ─────────────────────────────────────────────────────────────
  /** Re-draws the grid by applying both status and category filters to the cached list. */
  private void renderGrid() {
    auctionGrid.getChildren().clear();
    int column = 0;
    int row = 0;

    for (AuctionSummaryDTO dto : allAuctions) {
      if (!matchesStatusFilter(currentStatusFilter, dto.getStatus(), dto.getAuctionId())) continue;
      if (!matchesCategoryFilter(currentCategoryFilter, dto.getCategory())) continue;

      // Lấy từ cache nếu đã tạo trước đó – tránh load lại FXML khi chỉ đổi filter
      HBox card = cardCache.computeIfAbsent(dto.getAuctionId(), id -> {
        try {
          FXMLLoader loader =
              new FXMLLoader(getClass().getResource("/com/auction/client/view/ItemCard.fxml"));
          HBox node = loader.load();
          ItemCardController cardController = loader.getController();
          cardController.setData(
              CategoryUtils.iconFor(dto.getCategory()),
              dto.getCategory(),
              dto.getItemName(),
              dto.getCurrentPrice(),
              dto.getBidStep(),
              dto.getBidCount(),
              dto.getEndTime(),
              dto.getStatus(),
              dto.getAuctionId(),
              dto.getImageUrl(),
              dto.getHighestBidderName());
          return node;
        } catch (IOException e) {
          e.printStackTrace();
          return null;
        }
      });

      if (card == null) continue;
      auctionGrid.add(card, column++, row);
      GridPane.setMargin(card, new Insets(10));
      if (column == 2) {
        column = 0;
        row++;
      }
    }
  }

  // ── Filter helpers ────────────────────────────────────────────────────────
  /** Status filter: used by sidebar navigation (All / Active …). */
  private boolean matchesStatusFilter(String filterStatus, String status, String auctionId) {
    com.auction.share.enums.AuctionStatus auctionStatus = com.auction.share.enums.AuctionStatus.from(status);
    if (!auctionStatus.isVisible()) {
      return false;
    }
    if ("All".equalsIgnoreCase(filterStatus)) {
      return true;
    }
    return auctionStatus == com.auction.share.enums.AuctionStatus.from(filterStatus);
  }

  /**
   * Category filter: used by the filter bar on the Home view. Mapping from UI label → actual
   * category values stored in the DB.
   */
  private boolean matchesCategoryFilter(String categoryFilter, String category) {
    if (category == null) category = "";
    return switch (categoryFilter) {
      case "Antique" -> "Antique".equalsIgnoreCase(category);
      case "Art" -> "Art".equalsIgnoreCase(category);
      case "Electronic" -> "Electronic".equalsIgnoreCase(category);
      case "Jewelry" -> "Jewelry".equalsIgnoreCase(category);
      case "RealEstate" -> "RealEstate".equalsIgnoreCase(category);
      case "Vehicle" -> "Vehicle".equalsIgnoreCase(category);
      default -> true; // "All" or unknown → show everything
    };
  }

  // ── UI helpers ────────────────────────────────────────────────────────────
  private void updateFilterButtons(String activeCategory) {
    // Reset all to inactive, then highlight the active one
    Button[] allBtns = {btnAll, btnAntique, btnArt, btnElectronic, btnJewelry, btnRealEstate, btnVehicle};
    String[] labels = {"All", "Antique", "Art", "Electronic", "Jewelry", "RealEstate", "Vehicle"};

    for (int i = 0; i < allBtns.length; i++) {
      if (allBtns[i] != null) {
        allBtns[i].setStyle(labels[i].equals(activeCategory) ? STYLE_ACTIVE : STYLE_INACTIVE);
      }
    }
  }

  public String getCurrentStatusFilter() {
    return currentStatusFilter;
  }

}
