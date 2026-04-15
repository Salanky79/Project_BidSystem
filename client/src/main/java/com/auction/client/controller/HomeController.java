package com.auction.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    @FXML
    private GridPane auctionGrid;

    @FXML
    public void initialize() {
        String[] itemNames = {"iPhone 15 Pro", "Đồng hồ Rolex", "Túi xách Chanel", "Siêu xe Porsche", "Tranh Van Gogh", "Nhẫn kim cương"};
        String[] categories = {"Electronic", "Watch", "Hand Bag", "Car", "Fine Art", "Jewelry"};
        String[] icons = {"📱", "⌚", "👜", "🚗", "🖼", "💍"};
        double[] prices = {1200.0, 5500.0, 3200.0, 150000.0, 85000.0, 12000.0};

        int column = 0;
        int row = 0;

        try {
            for (int i = 0; i < itemNames.length; i++) {
                // SỬA TẠI ĐÂY: Đường dẫn phải đầy đủ từ thư mục resources
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/ItemCard.fxml"));
                HBox card = loader.load();

                ItemCardController cardController = loader.getController();
                cardController.setData(icons[i], categories[i], itemNames[i], prices[i], 0, "12 D 05 Hrs");

                if (column == 2) {
                    column = 0;
                    row++;
                }

                auctionGrid.add(card, column++, row);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi nạp ItemCard.fxml: " + e.getMessage());
        }
    }
    @FXML
    public void handleHome(javafx.event.ActionEvent event) {

        System.out.println("Nút Home đã được nhấn!");

    }

    public void handleLogout(javafx.event.ActionEvent event) {
        try {
            // SỬA TẠI ĐÂY: Đường dẫn về màn hình Login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/Login.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi: Không tìm thấy file Login.fxml!");
        }
    }

    public void handleCardClick(javafx.scene.input.MouseEvent mouseEvent) {
        try {
            // Chỗ này bạn đã sửa đúng
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/AuctionDetail.fxml"));
            Parent root = loader.load();

            Stage detailStage = new Stage();
            detailStage.setTitle("Chi tiết sản phẩm đấu giá");
            detailStage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root);
            detailStage.setScene(scene);
            detailStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi: Không tìm thấy file AuctionDetail.fxml!");
        }
    }
}