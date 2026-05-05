package com.auction.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class HomeController extends HomeFrameController {

// Kéo cái lưới trống từ giao diện FXML vào đây để điều khiển
    @FXML
    private GridPane auctionGrid;
    @FXML
    private VBox Content;

    // 1. Tạo một mảng dữ liệu giả lập (Sau này bạn sẽ lấy List<Item> từ Server trả về)
    String[] itemNames = {"iPhone 15 Pro", "Đồng hồ Rolex", "Túi xách Chanel", "Siêu xe Porsche", "Tranh Van Gogh", "Nhẫn kim cương"};
    String[] categories = {"Electronic", "Watch", "Hand Bag", "Car", "Fine Art", "Jewelry"};
    String[] icons = {"📱", "⌚", "👜", "🚗", "🖼", "💍"};
    double[] prices = {1200.0, 5500.0, 3200.0, 150000.0, 85000.0, 12000.0};
    String[] status = {"Active", "End", "Cancelled", "In Queue", "Active", "End"};

    @FXML
    public void initialize() {
        loadAuction("All");
    }

    public void loadAuction(String filterStatus) {
        auctionGrid.getChildren().clear();

        int column = 0;
        int row = 0;

        try {
            for (int i = 0; i < itemNames.length; i++) {

                // KIỂM TRA ĐIỀU KIỆN LỌC
                // Nếu filter là "All" HOẶC status trùng với filter thì mới vẽ
                if (filterStatus.equals("All") || status[i].equalsIgnoreCase(filterStatus)) {

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/ItemCard.fxml"));
                    HBox card = loader.load();

                    ItemCardController cardController = loader.getController();
                    cardController.setData(icons[i], categories[i], itemNames[i], prices[i], 0, "12 D 05 Hrs", status[i]);

                    if (column == 2) {
                        column = 0;
                        row++;
                    }
                    auctionGrid.add(card, column++, row);

                    // Thêm khoảng cách giữa các card cho đẹp
                    GridPane.setMargin(card, new Insets(10));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
