package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.IOException;

/**
 * Controller cho SellerDashboard.fxml
 * Quản lý: stat boxes, filter bar, auction card grid
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
    public void handleFilterAll(ActionEvent event) {
        setStatusFilter("All");
        loadAuctionCards("All");
    }

    @FXML
    public void handleFilterActive(ActionEvent event) {
        setStatusFilter("Active");
        loadAuctionCards("Active");
    }

    @FXML
    public void handleFilterEnded(ActionEvent event) {
        setStatusFilter("Ended");
        loadAuctionCards("Ended");
    }

    @FXML
    public void handleDateMonth(ActionEvent event) {
        setDateFilter("Month");
    }

    @FXML
    public void handleDate7Days(ActionEvent event) {
        setDateFilter("7Days");
    }

    @FXML
    public void handleManage(ActionEvent event) {
        System.out.println("Open manage panel.");
    }

    // ===== LOAD CARDS =====

    private void loadAuctionCards(String filterStatus) {
        auctionGrid.getChildren().clear();

        int col = 0, row = 0;

        // TODO: Thay bằng dữ liệu thật từ service
        // Ví dụ gọi: auctionService.getSellerAuctions(response -> { ... })
        // Dưới đây là placeholder để test layout

        /* Ví dụ khi có data:
        for (AuctionSummaryDTO dto : dtoList) {
            if (!matchesFilter(filterStatus, dto.getStatus())) continue;
            try {
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/auction/client/view/SellerItemCard.fxml")
                );
                HBox card = loader.load();
                SellerItemCardController ctrl = loader.getController();
                ctrl.setData(dto);

                auctionGrid.add(card, col++, row);
                GridPane.setMargin(card, new Insets(0));
                if (col == 2) { col = 0; row++; }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        */
    }

    private boolean matchesFilter(String filter, String status) {
        return "All".equalsIgnoreCase(filter) || status.equalsIgnoreCase(filter);
    }

    // ===== STYLE HELPERS =====

    private static final String STYLE_ACTIVE =
            "-fx-background-color: #1a1a1a; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-border-radius: 6; -fx-background-radius: 6; " +
                    "-fx-padding: 4 12 4 12; -fx-cursor: hand;";

    private static final String STYLE_INACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #555555; " +
                    "-fx-font-size: 12px; -fx-border-radius: 6; -fx-background-radius: 6; " +
                    "-fx-padding: 4 12 4 12; -fx-cursor: hand; " +
                    "-fx-border-color: #cccccc; -fx-border-width: 1;";

    private void setStatusFilter(String selected) {
        btnFilterAll.setStyle("All".equals(selected)    ? STYLE_ACTIVE : STYLE_INACTIVE);
        btnFilterActive.setStyle("Active".equals(selected) ? STYLE_ACTIVE : STYLE_INACTIVE);
        btnFilterEnded.setStyle("Ended".equals(selected)  ? STYLE_ACTIVE : STYLE_INACTIVE);
    }

    private void setDateFilter(String selected) {
        btnDateMonth.setStyle("Month".equals(selected)   ? STYLE_ACTIVE : STYLE_INACTIVE);
        btnDate7Days.setStyle("7Days".equals(selected)   ? STYLE_ACTIVE : STYLE_INACTIVE);
    }
}