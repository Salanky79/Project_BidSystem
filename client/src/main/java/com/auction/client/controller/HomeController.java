package com.auction.client.controller;

import com.auction.client.ClientContext;
import com.auction.client.service.AuctionService;
import com.auction.share.DTO.AuctionSummaryDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.auction.client.service.WatchlistService;

public class HomeController extends HomeFrameController {

    private final AuctionService auctionService = ClientContext.auctionService();

    // ── FXML bindings ──────────────────────────────────────────────────────────
    @FXML private GridPane auctionGrid;
    @FXML private VBox Content;

    // Category filter buttons
    @FXML private Button btnAll;
    @FXML private Button btnJewelry;
    @FXML private Button btnWatches;
    @FXML private Button btnBags;
    @FXML private Button btnFineArt;
    @FXML private Button btnCars;
    @FXML private Button btnOthers;

    // ── State ──────────────────────────────────────────────────────────────────
    /** Cached list of all auctions fetched from server (avoids repeated network calls). */
    private final List<AuctionSummaryDTO> allAuctions = new ArrayList<>();

    /** Current status filter set by sidebar (e.g. "All", "Active", "Watchlist"). */
    private String currentStatusFilter = "All";

    /** Current category filter set by the filter bar (e.g. "All", "Jewelry", "Cars"). */
    private String currentCategoryFilter = "All";

    // ── Button style constants ─────────────────────────────────────────────────
    private static final String STYLE_ACTIVE =
            "-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-font-size: 13px; " +
            "-fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;";
    private static final String STYLE_INACTIVE =
            "-fx-background-color: #e0e0e0; -fx-text-fill: #333333; -fx-font-size: 13px; " +
            "-fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;";

    // ── Initializer ───────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        // "All" is active by default (matches the initial FXML dark style)
        updateFilterButtons("All");
    }

    // ── Public API called by HomeFrameController ───────────────────────────────
    /**
     * Fetches auctions from server filtered by status, then renders the grid.
     * Resets category filter to "All" whenever the sidebar navigation changes.
     */
    public void loadAuction(String filterStatus) {
        this.currentStatusFilter = filterStatus;
        this.currentCategoryFilter = "All";
        updateFilterButtons("All");

        allAuctions.clear();
        auctionGrid.getChildren().clear();

        auctionService.getAuctions(response -> Platform.runLater(() -> {
            if (response != null && response.isSuccess() && response.getData() instanceof List<?> list) {
                for (Object obj : list) {
                    if (obj instanceof AuctionSummaryDTO dto) {
                        allAuctions.add(dto);
                    }
                }
                renderGrid();
            } else {
                System.out.println("Failed to load auctions: " +
                        (response != null ? response.getMessage() : "Unknown error"));
            }
        }));
    }

    // ── Category filter handlers ───────────────────────────────────────────────
    @FXML private void handleFilterAll()     { applyCategory("All"); }
    @FXML private void handleFilterJewelry() { applyCategory("Jewelry"); }
    @FXML private void handleFilterWatches() { applyCategory("Watches"); }
    @FXML private void handleFilterBags()    { applyCategory("Bags"); }
    @FXML private void handleFilterFineArt() { applyCategory("Fine Art"); }
    @FXML private void handleFilterCars()    { applyCategory("Cars"); }
    @FXML private void handleFilterOthers()  { applyCategory("Others"); }

    private void applyCategory(String category) {
        currentCategoryFilter = category;
        updateFilterButtons(category);
        renderGrid();   // Re-render from cache – no extra network call
    }

    // ── Rendering ─────────────────────────────────────────────────────────────
    /** Re-draws the grid by applying both status and category filters to the cached list. */
    private void renderGrid() {
        auctionGrid.getChildren().clear();
        int column = 0;
        int row    = 0;

        for (AuctionSummaryDTO dto : allAuctions) {
            if (!matchesStatusFilter(currentStatusFilter, dto.getStatus(), dto.getAuctionId())) continue;
            if (!matchesCategoryFilter(currentCategoryFilter, dto.getCategory()))               continue;

            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/auction/client/view/ItemCard.fxml"));
                HBox card = loader.load();

                ItemCardController cardController = loader.getController();
                cardController.setData(
                        iconForCategory(dto.getCategory()),
                        dto.getCategory(),
                        dto.getItemName(),
                        dto.getCurrentPrice(),
                        dto.getBidStep(),
                        0,
                        dto.getStartTime(),
                        dto.getEndTime(),
                        dto.getStatus(),
                        dto.getAuctionId());

                auctionGrid.add(card, column++, row);
                GridPane.setMargin(card, new Insets(10));
                if (column == 2) { column = 0; row++; }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ── Filter helpers ────────────────────────────────────────────────────────
    /**
     * Status filter: used by sidebar navigation (All / Active / Watchlist …).
     */
    private boolean matchesStatusFilter(String filterStatus, String status, String auctionId) {
        if ("Watchlist".equalsIgnoreCase(filterStatus)) {
            return WatchlistService.getInstance().isFollowed(auctionId);
        }
        if ("DRAFT".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
            return false;
        }
        return "All".equalsIgnoreCase(filterStatus) || status.equalsIgnoreCase(filterStatus);
    }

    /**
     * Category filter: used by the filter bar on the Home view.
     * Mapping from UI label → actual category values stored in the DB.
     */
    private boolean matchesCategoryFilter(String categoryFilter, String category) {
        if (category == null) category = "";
        String finalCategory = category;
        return switch (categoryFilter) {
            case "Jewelry"  -> "Jewelry".equalsIgnoreCase(category);
            case "Watches"  -> "Watch".equalsIgnoreCase(category);
            case "Bags"     -> "Hand Bag".equalsIgnoreCase(category) || "Clothing".equalsIgnoreCase(category);
            case "Fine Art" -> "Art".equalsIgnoreCase(category);
            case "Cars"     -> "Car".equalsIgnoreCase(category);
            case "Others"   -> !List.of("Jewelry", "Watch", "Hand Bag", "Clothing", "Art", "Car")
                                    .stream().anyMatch(c -> c.equalsIgnoreCase(finalCategory));
            default         -> true;   // "All" or unknown → show everything
        };
    }

    // ── UI helpers ────────────────────────────────────────────────────────────
    private void updateFilterButtons(String activeCategory) {
        // Reset all to inactive, then highlight the active one
        Button[] allBtns  = { btnAll, btnJewelry, btnWatches, btnBags, btnFineArt, btnCars, btnOthers };
        String[] labels   = { "All",  "Jewelry",  "Watches",  "Bags", "Fine Art", "Cars", "Others"  };

        for (int i = 0; i < allBtns.length; i++) {
            if (allBtns[i] != null) {
                allBtns[i].setStyle(labels[i].equals(activeCategory) ? STYLE_ACTIVE : STYLE_INACTIVE);
            }
        }
    }

    private String iconForCategory(String category) {
        if (category == null) return "📦";
        return switch (category) {
            case "Electronic" -> "📱";
            case "Watch"      -> "⌚";
            case "Hand Bag", "Clothing" -> "👜";
            case "Car"        -> "🚗";
            case "Art"        -> "🖼";
            case "Jewelry"    -> "💍";
            default           -> "📦";
        };
    }
}

