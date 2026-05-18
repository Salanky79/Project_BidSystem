package com.auction.client.controller;

import com.auction.client.ClientContext;
import com.auction.client.service.UserService;
import com.auction.share.DTO.LoginRequest;
import com.auction.share.DTO.Response;
import com.auction.share.DTO.UserDTO;
import com.auction.share.exceptions.ValidationException;
import javafx.application.Platform;
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

import java.io.IOException;

public class LoginController {
    private final UserService userService = ClientContext.userService();

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

    @FXML
    public void initialize() {
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());
        passwordVisible.setVisible(false);
        passwordField.setVisible(true);
    }

    @FXML
    public void togglePassword() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            passwordVisible.setVisible(true);
            passwordField.setVisible(false);
            eyeToggleBtn.setText("\uD83D\uDE48");
        } else {
            passwordVisible.setVisible(false);
            passwordField.setVisible(true);
            eyeToggleBtn.setText("\uD83D\uDC41");
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = isPasswordVisible ? passwordVisible.getText() : passwordField.getText();

        try {
            userService.login(new LoginRequest(username, password), response -> Platform.runLater(() -> {
                if (true) {
                    try {
                        String role = null;
                        if (response.getData() instanceof UserDTO userDTO) {
                            role = userDTO.getRole();
                        }

                        if ("seller".equalsIgnoreCase(role)) {
                            loadSellerDashboard(event);
                        } else {
                            loadHome(event);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    errorLabel.setText(extractErrorMessage(response));
                    errorLabel.setVisible(true);
                }
            }));
            errorLabel.setVisible(false);
        } catch (ValidationException e) {
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
        }
    }


    private static String extractErrorMessage(Response<?> response) {
        if (response != null && response.getMessage() != null && !response.getMessage().isBlank()) {
            return response.getMessage();
        }
        return "Khong the ket noi Server!";
    }

    public void handleSignup(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/SignupView.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Loi: Khong tim thay file SignupView.fxml!");
        }
    }

    protected void loadHome(ActionEvent event) {
        try {
            FXMLLoader frameLoader = new FXMLLoader(getClass().getResource("/com/auction/client/view/HomeFrame.fxml"));
            Parent homeFrameRoot = frameLoader.load();
            HomeFrameController frameController = frameLoader.getController();

            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/com/auction/client/view/Home.fxml"));
            Node homeNode = homeLoader.load();
            frameController.setView(homeNode);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(homeFrameRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Loi giao dien: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }

    protected void loadSellerDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/SellerDashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Loi giao dien: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }
}
