package com.auction.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
    String[] itemNames = {"iPhone 15 Pro", "Rolex Watch", "Chanel Handbag", "Porsche Supercar", "Van Gogh Painting", "Diamond Ring"};
    String[] categories = {"Electronic", "Watch", "Hand Bag", "Car", "Fine Art", "Jewelry"};
    String[] icons = {"📱", "⌚", "👜", "🚗", "🖼", "💍"};
    double[] prices = {1200.0, 5500.0, 3200.0, 150000.0, 85000.0, 12000.0};
    String[] status = {"Active", "End", "Cancelled", "In Queue", "Active", "End"};

    @FXML
    public void initialize() {
        // Biến để tính toán tọa độ (Cột và Hàng) nhét vào lưới
        int column = 0;
        int row = 0;

        try {
            // 2. Vòng lặp để đẻ ra 6 cái thẻ sản phẩm
            for (int i = 0; i < itemNames.length; i++) {

                // Lấy cái "Khuôn đúc" ItemCard.fxml ra
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/ItemCard.fxml"));
                HBox card = loader.load(); // Vì thẻ của bạn bọc ngoài bằng HBox

                // Lấy "Bác thợ" điều khiển khuôn đúc để đắp dữ liệu vào
                ItemCardController cardController = loader.getController();
                cardController.setData(icons[i], categories[i], itemNames[i], prices[i], 0, "12 D 05 Hrs", status[i]);

                // --- Thuật toán xếp gạch vào lưới 2 cột ---
                if (column == 2) { // Nếu đã xếp đủ 2 cột (0 và 1) thì xuống dòng
                    column = 0;
                    row++;
                }

                // Nhét cái thẻ vừa nặn xong vào đúng vị trí Cột và Hàng trên lưới
                auctionGrid.add(card, column++, row);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: ItemCard.fxml file not found. Please check the path.");
        }
    }

}
