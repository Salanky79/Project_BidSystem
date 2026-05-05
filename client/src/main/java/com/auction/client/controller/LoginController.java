package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.application.Platform;
import com.auction.client.network.NetworkClient;
import com.auction.client.network.RestClient;

import java.io.IOException;

public class LoginController {

    // ==========================================
    // PHẦN 1: KHAI BÁO CÁC BIẾN CHO NÚT CON MẮT VÀ ĐĂNG NHẬP
    // ==========================================
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

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
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username và Password không được để trống!");
            errorLabel.setVisible(true);
            return;
        }

        // Tạm ẩn lỗi trước khi gửi request
        errorLabel.setVisible(false);

        // Gửi request thông qua RestClient (HTTP API)
        RestClient.getInstance().login(username, password, response -> {
            // Cập nhật giao diện thì phải nằm trong Platform.runLater
            Platform.runLater(() -> {
                if (response != null && response.contains("\"status\":\"SUCCESS\"")) {
                    try {
                        // Khởi động TCP Socket sau khi đăng nhập thành công
                        NetworkClient.getInstance();

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
                        errorLabel.setText("Lỗi giao diện: " + e.getMessage());
                        errorLabel.setVisible(true);
                    }
                } else {
                    // Hiển thị lỗi từ server
                    String errorMsg = "Không thể kết nối Server!";
                    if (response != null && response.contains("\"message\":\"")) {
                        // Trích xuất chuỗi lỗi đơn giản
                        int start = response.indexOf("\"message\":\"") + 11;
                        int end = response.indexOf("\"", start);
                        if (end > start) {
                            errorMsg = response.substring(start, end);
                        }
                    }
                    errorLabel.setText(errorMsg);
                    errorLabel.setVisible(true);
                }
            });
        });
    }

    public void handleSignup(ActionEvent event) {
        try {
            // 1. Tải lại file giao diện Login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/SignupView.fxml"));
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
            System.out.println("Lỗi: Không tìm thấy file SignupView.fxml!");
        }
    }
}