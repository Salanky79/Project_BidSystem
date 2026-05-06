package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;

import java.io.IOException;

/**
 * Controller cho màn hình đăng ký HanoiBid.
 * Liên kết với SignupView.fxml
 */
public class SignupController {

    @FXML private TextField     fullnameField;
    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label fullnameError;
    @FXML private Label usernameError;
    @FXML private Label passwordError;
    @FXML private Label confirmError;
    @FXML private Label statusLabel;
    
    @FXML private VBox sellerCard;
    @FXML private VBox bidderCard;
    @FXML private ComboBox<String> roleCombo;

    /** Gọi khi người dùng nhấn nút SIGN UP */
    @FXML
    private void handleSignup() {
        boolean valid = true;

        // --- Validate Full Name ---
        if (fullnameField.getText().trim().isEmpty()) {
            show(fullnameError, true);
            highlight(fullnameField, true);
            valid = false;
        } else {
            show(fullnameError, false);
            highlight(fullnameField, false);
        }

        // --- Validate Username ---
        if (usernameField.getText().trim().isEmpty()) {
            show(usernameError, true);
            highlight(usernameField, true);
            valid = false;
        } else {
            show(usernameError, false);
            highlight(usernameField, false);
        }

        // --- Validate Password ---
        if (passwordField.getText().isEmpty()) {
            show(passwordError, true);
            highlight(passwordField, true);
            valid = false;
        } else {
            show(passwordError, false);
            highlight(passwordField, false);
        }

        // --- Validate Confirm Password ---
        if (!confirmPasswordField.getText().equals(passwordField.getText())
                || confirmPasswordField.getText().isEmpty()) {
            show(confirmError, true);
            highlight(confirmPasswordField, true);
            valid = false;
        } else {
            show(confirmError, false);
            highlight(confirmPasswordField, false);
        }

        // --- Result ---
        if (valid) {
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #228B22;");
            statusLabel.setText("Đăng ký thành công! Chào mừng, "
                    + fullnameField.getText().trim() + " 🎉");
            
            com.auction.client.factory.RoleUIFactory factory;
            if ("Seller".equals(roleCombo.getValue())) {
                factory = new com.auction.client.factory.SellerUIFactory();
            } else {
                factory = new com.auction.client.factory.BidderUIFactory();
            }
            
            try {
                com.auction.client.factory.DashboardProduct dashboard = factory.createDashboard();
                Scene scene = dashboard.getScene();
                Stage stage = (Stage) statusLabel.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle(dashboard.getTitle());
                stage.centerOnScreen();
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                statusLabel.setText("Lỗi khi chuyển màn hình!");
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #cc3333;");
            }
        } else {
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #cc3333;");
            statusLabel.setText("Vui lòng điền đầy đủ các trường bắt buộc.");
        }
    }

    // ── helpers ──────────────────────────────────────────────
    private void show(Label label, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
    }

    private void highlight(javafx.scene.control.Control ctrl, boolean error) {
        String base = "-fx-background-color: #ffffff; -fx-border-width: 1; "
                    + "-fx-border-radius: 6; -fx-background-radius: 6; "
                    + "-fx-font-size: 14px; -fx-padding: 10 14 10 14; -fx-pref-height: 42;";
        if (error) {
            ctrl.setStyle(base + "-fx-border-color: #cc3333;");
        } else {
            ctrl.setStyle(base + "-fx-border-color: #cccccc;");
        }
    }

    public void handleLogin(ActionEvent event) {
        System.out.println("Trở về màn hình Login!");
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

    private final String defaultStyle = "-fx-border-color:#cccccc; -fx-border-width:1.5; " +
            "-fx-border-radius:8; -fx-background-radius:8; " +
            "-fx-background-color:#ffffff; -fx-cursor:hand; -fx-padding:10;";
            
    private final String selectedStyle = "-fx-border-color:#3366cc; -fx-border-width:1.5; " +
            "-fx-border-radius:8; -fx-background-radius:8; " +
            "-fx-background-color:#e6f0ff; -fx-cursor:hand; -fx-padding:10;";

    public void selectSeller(MouseEvent mouseEvent) {
        sellerCard.setStyle(selectedStyle);
        bidderCard.setStyle(defaultStyle);
        if (roleCombo != null) {
            roleCombo.setValue("Seller");
        }
    }

    public void selectBidder(MouseEvent mouseEvent) {
        bidderCard.setStyle(selectedStyle);
        sellerCard.setStyle(defaultStyle);
        if (roleCombo != null) {
            roleCombo.setValue("Bidder");
        }
    }
}
