package com.auction.client.controller;

import com.auction.client.ClientContext;
import com.auction.client.service.AuctionService;
import com.auction.share.DTO.AuctionSummaryDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class HomeController extends HomeFrameController {
    private final AuctionService auctionService = ClientContext.auctionService();

// Kéo cái lưới trống từ giao diện FXML vào đây để điều khiển
    @FXML
    private GridPane auctionGrid;
    @FXML
    private VBox Content;

    @FXML
    public void initialize() {
        loadAuction("All");
    }

    public void loadAuction(String filterStatus) {
        auctionGrid.getChildren().clear();

        auctionService.getAuctions(response -> Platform.runLater(() -> {
            if (response != null && response.isSuccess() && response.getData() instanceof List<?> list) {
                int column = 0;
                int row = 0;

                for (Object obj : list) {
                    if (obj instanceof AuctionSummaryDTO dto) {
                        if (filterStatus.equals("All") || dto.getStatus().equalsIgnoreCase(filterStatus)) {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/ItemCard.fxml"));
                                HBox card = loader.load();

                                ItemCardController cardController = loader.getController();
                                // Temporary icon mapping based on category for UI consistency
                                String icon = "📦";
                                if ("Electronic".equalsIgnoreCase(dto.getCategory())) icon = "📱";
                                else if ("Watch".equalsIgnoreCase(dto.getCategory())) icon = "⌚";
                                else if ("Hand Bag".equalsIgnoreCase(dto.getCategory()) || "Clothing".equalsIgnoreCase(dto.getCategory())) icon = "👜";
                                else if ("Car".equalsIgnoreCase(dto.getCategory())) icon = "🚗";
                                else if ("Art".equalsIgnoreCase(dto.getCategory()) || "Art".equalsIgnoreCase(dto.getCategory())) icon = "🖼";
                                else if ("Jewelry".equalsIgnoreCase(dto.getCategory())) icon = "💍";
                                
                                cardController.setData(icon, dto.getCategory(), dto.getItemName(), dto.getCurrentPrice(), 0, dto.getEndTime(), dto.getStatus(), dto.getAuctionId());

                                if (column == 2) {
                                    column = 0;
                                    row++;
                                }
                                auctionGrid.add(card, column++, row);
                                GridPane.setMargin(card, new Insets(10));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } else {
                System.out.println("Failed to load auctions: " + (response != null ? response.getMessage() : "Unknown error"));
            }
        }));
    }

}
