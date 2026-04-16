package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.auction.client.controller.ItemCardController;

import java.io.IOException;

public class HomeController {

// Kéo cái lưới trống từ giao diện FXML vào đây để điều khiển
    @FXML
    private GridPane auctionGrid;

    @FXML
    public void initialize() {
        // 1. Tạo một mảng dữ liệu giả lập (Sau này bạn sẽ lấy List<Item> từ Server trả về)
        String[] itemNames = {"iPhone 15 Pro", "Đồng hồ Rolex", "Túi xách Chanel", "Siêu xe Porsche", "Tranh Van Gogh", "Nhẫn kim cương"};
        String[] categories = {"Electronic", "Watch", "Hand Bag", "Car", "Fine Art", "Jewelry"};
        String[] icons = {"📱", "⌚", "👜", "🚗", "🖼", "💍"};
        double[] prices = {1200.0, 5500.0, 3200.0, 150000.0, 85000.0, 12000.0};

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
                cardController.setData(icons[i], categories[i], itemNames[i], prices[i], 0, "12 D 05 Hrs");

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
            System.out.println("Lỗi không tìm thấy file ItemCard.fxml. Hãy kiểm tra lại đường dẫn!");
        }
    }

    public void handleLogout(javafx.event.ActionEvent event) {
        System.out.println("Đang đăng xuất... Trở về màn hình Login!");
        try {
            // 1. Tải lại file giao diện Login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/Login.fxml"));
            Parent loginRoot = loader.load();

            // 2. Lấy cái cửa sổ (Stage) hiện tại đang hiển thị
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            // 3. Đổi cảnh sang Login
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi: Không tìm thấy file Login.fxml!");
        }
    }

    public void handleYourListing(ActionEvent actionEvent) {
        try {
            // 1. Tải lại file giao diện Addlisting
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/addlisting.fxml"));
            Parent root = loader.load();

            // 2. Tạo một Stage mới (Cửa sổ mới)
            Stage newStage = new Stage();
            newStage.setTitle("Thêm sản phẩm mới");

            // 3. Đưa giao diện vào Stage và hiển thị
            Scene scene = new Scene(root);
            newStage.setScene(scene);

            // Hiển thị cửa sổ mới, lúc này Home.fxml vẫn đang chạy ở Stage cũ
            newStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi: Không tìm thấy file addlisting.fxml!");
        }
    }


    public void handleHome(ActionEvent actionEvent) {
    }

    public void handleProfile(ActionEvent actionEvent) {
        try {
            // 1. Tải lại file giao diện Addlisting
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/profile.fxml"));
            Parent root = loader.load();

            // 2. Tạo một Stage mới (Cửa sổ mới)
            Stage newStage = new Stage();
            newStage.setTitle("user");

            // 3. Đưa giao diện vào Stage và hiển thị
            Scene scene = new Scene(root);
            newStage.setScene(scene);

            // Hiển thị cửa sổ mới, lúc này Home.fxml vẫn đang chạy ở Stage cũ
            newStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi: Không tìm thấy file profile.fxml!");
        }
    }
}