package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    // ==========================================
    // PHẦN 1: KHAI BÁO CÁC BIẾN CHO NÚT CON MẮT
    // ==========================================
    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordVisible;

    @FXML
    private Button eyeToggleBtn;

    private boolean isPasswordVisible = false;

    // Hàm này tự động chạy khi màn hình Login vừa bật lên
    @FXML
    public void initialize() {
        // Đồng bộ chữ giữa ô giấu kín và ô hiện chữ
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());

        // Mặc định giấu ô chữ, hiện ô dấu chấm
        passwordVisible.setVisible(false);
        passwordField.setVisible(true);
    }

    // ==========================================
    // PHẦN 2: XỬ LÝ SỰ KIỆN BẤM NÚT CON MẮT
    // ==========================================
    @FXML
    public void togglePassword(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible; // Đảo trạng thái

        if (isPasswordVisible) {
            // Mở mắt: Hiện chữ, giấu chấm
            passwordVisible.setVisible(true);
            passwordField.setVisible(false);
            eyeToggleBtn.setText("🙈"); // Đổi icon
        } else {
            // Nhắm mắt: Giấu chữ, hiện chấm
            passwordVisible.setVisible(false);
            passwordField.setVisible(true);
            eyeToggleBtn.setText("👁"); // Đổi lại icon
        }
    }

    // ==========================================
    // PHẦN 3: XỬ LÝ SỰ KIỆN BẤM NÚT LOGIN
    // ==========================================
    @FXML
    public void handleLogin(ActionEvent event) {
        try {
            // 1. Tải file Home.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/Home.fxml"));
            Parent homeRoot = loader.load();

            // 2. Lấy Stage hiện tại (cửa sổ đang hiển thị)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 3. Tạo Scene mới với nội dung là Home.fxml
            Scene homeScene = new Scene(homeRoot);

            // 4. Đặt Scene mới lên Stage và hiển thị
            stage.setScene(homeScene);
            stage.centerOnScreen(); // Căn giữa lại màn hình vì Home thường to hơn Login
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Không tìm thấy file Home.fxml hoặc lỗi nạp file!");
        }
    }
}