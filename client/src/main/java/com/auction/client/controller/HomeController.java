package com.auction.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class HomeController extends HomeFrameController {
    private static final String ITEM_CARD_VIEW = "/com/auction/client/view/ItemCard.fxml";
    private static final String FILTER_ALL = "All";
    private static final String DEFAULT_TIME_LEFT = "12 D 05 Hrs";

    @FXML
    private GridPane auctionGrid;
    @FXML
    private VBox Content;

    private final String[] itemNames = {
            "iPhone 15 Pro", "Rolex Watch", "Chanel Handbag",
            "Porsche Supercar", "Van Gogh Painting", "Diamond Ring"
    };
    private final String[] categories = {"Electronic", "Watch", "Hand Bag", "Car", "Fine Art", "Jewelry"};
    private final String[] icons = {"", "", "", "", "", ""};
    private final double[] prices = {1200.0, 5500.0, 3200.0, 150000.0, 85000.0, 12000.0};
    private final String[] status = {"Active", "End", "Cancelled", "In Queue", "Active", "End"};

    @FXML
    public void initialize() {
        loadAuction(FILTER_ALL);
    }

    public void loadAuction(String filterStatus) {
        auctionGrid.getChildren().clear();

        int column = 0;
        int row = 0;

        try {
            for (int i = 0; i < itemNames.length; i++) {
                if (shouldRender(filterStatus, status[i])) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(ITEM_CARD_VIEW));
                    HBox card = loader.load();

                    ItemCardController cardController = loader.getController();
                    cardController.setData(
                            icons[i], categories[i], itemNames[i],
                            prices[i], 0, DEFAULT_TIME_LEFT, status[i]
                    );

                    if (column == 2) {
                        column = 0;
                        row++;
                    }
                    auctionGrid.add(card, column++, row);
                    GridPane.setMargin(card, new Insets(10));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: ItemCard.fxml file not found. Please check the path.");
        }
    }

    private boolean shouldRender(String filterStatus, String currentStatus) {
        return FILTER_ALL.equals(filterStatus) || currentStatus.equalsIgnoreCase(filterStatus);
    }
}

