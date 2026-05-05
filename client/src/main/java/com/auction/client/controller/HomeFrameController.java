package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.IOException;

public class HomeFrameController {
    
    @FXML
    private ScrollPane scrollContent;
    private HomeController currentHomeController;

    public void setView(Node node) {
        scrollContent.setContent(node);
    }

    public void handleLogout(ActionEvent event) {
        System.out.println("Đang đăng xuất... Trở về màn hình Login!");
        try {
            // 1. Tải lại file giao diện Login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/Login.fxml"));
            Parent loginRoot = loader.load();

            // 2. Lấy cái cửa sổ (Stage) hiện tại đang hiển thị
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

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

    public void handleSell() {
        changeView("/com/auction/client/view/Sell.fxml");
    }
    public void handleProfile() {
        changeView("/com/auction/client/view/profile.fxml");
    }
    public void handleActiveListings() {
        loadHomePage();
        if (currentHomeController != null) {
            currentHomeController.loadAuction("Active");
        }
    }
    private void changeView(String fxmlFile) {
        try {
            // 1. Tải file FXML của trang mới (ví dụ: ItemDetail.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node newNode = loader.load();

            scrollContent.setContent(newNode);
            currentHomeController = null;

            // Nếu muốn dãn hết chiều ngang (thường VBox đã mặc định Fill Width)
            if (newNode instanceof Region) {
                ((Region) newNode).setMaxWidth(Double.MAX_VALUE);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi: Không tìm thấy file " + fxmlFile);
        }
    }

    public void handleHome(ActionEvent actionEvent) {
        loadHomePage(); // Sử dụng hàm loadHomePage thay vì changeView
        if (currentHomeController != null) {
            currentHomeController.loadAuction("All"); // Hiện tất cả khi về Home
        }
    }

    public void handleYourListing(ActionEvent actionEvent) {

    }
    public void loadHomePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/Home.fxml"));
            Node node = loader.load();

            // CỰC KỲ QUAN TRỌNG: Phải lấy controller mới sau mỗi lần load
            currentHomeController = loader.getController();

            scrollContent.setContent(node);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi: Không load được Home.fxml");
        }
    }
}
