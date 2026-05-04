package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeFrameController {
    
    @FXML
    private ScrollPane scrollContent;

    public void setView(Node node) {
        scrollContent.setContent(node);
    }

    public void handleLogout(ActionEvent event) {
        System.out.println("Logging out... Returning to Login screen.");
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
            System.out.println("Error: Login.fxml file not found.");
        }
    }

    public void handleYourListing(ActionEvent actionEvent) {
        changeView("/com/auction/client/view/addlisting.fxml");
    }
    public void handleProfile() {
        changeView("/com/auction/client/view/profile.fxml");
    }
    private void changeView(String fxmlFile) {
        try {
            // 1. Tải file FXML của trang mới (ví dụ: ItemDetail.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node newNode = loader.load();

            scrollContent.setContent(newNode);

            // Nếu muốn dãn hết chiều ngang (thường VBox đã mặc định Fill Width)
            if (newNode instanceof Region) {
                ((Region) newNode).setMaxWidth(Double.MAX_VALUE);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: File not found " + fxmlFile);
        }
    }

    public void handleHome(ActionEvent actionEvent) {
        changeView("/com/auction/client/view/Home.fxml");
    }
}
