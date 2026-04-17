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
    private void handleLogin(ActionEvent event) {
        try {
            // 1. Load cái KHUNG (HomeFrame)
            FXMLLoader frameLoader = new FXMLLoader(getClass().getResource("/com/auction/client/view/HomeFrame.fxml"));
            Parent homeFrameRoot = frameLoader.load();

            // 2. Lấy Controller của HomeFrame để tí nữa còn ra lệnh
            HomeFrameController frameController = frameLoader.getController();

            // 3. Load cái nội dung HOME
            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/com/auction/client/view/Home.fxml"));
            Node homeNode = homeLoader.load();

            // 4. Đẩy Home vào ScrollPane của HomeFrame
            frameController.setView(homeNode);

            // 5. Hiển thị HomeFrame lên cửa sổ (Stage)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(homeFrameRoot));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}