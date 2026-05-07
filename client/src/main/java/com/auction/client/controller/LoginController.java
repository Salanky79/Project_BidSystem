package com.auction.client.controller;

import com.auction.client.network.RestClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
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
    private static final String SIGNUP_VIEW = "/com/auction/client/view/SignupView.fxml";
    private static final String HOME_FRAME_VIEW = "/com/auction/client/view/HomeFrame.fxml";
    private static final String HOME_VIEW = "/com/auction/client/view/Home.fxml";
    private static final String REQUIRED_CREDENTIALS_MESSAGE = "Username vÃ  Password khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng!";
    private static final String SERVER_UNREACHABLE_MESSAGE = "KhÃ´ng thá»ƒ káº¿t ná»‘i Server!";
    private static final String SIGNUP_VIEW_NOT_FOUND_MESSAGE = "Lá»—i: KhÃ´ng tÃ¬m tháº¥y file SignupView.fxml!";

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
    public void togglePassword(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;
        setPasswordVisibility(isPasswordVisible);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText(REQUIRED_CREDENTIALS_MESSAGE);
            errorLabel.setVisible(true);
            return;
        }

        errorLabel.setVisible(false);

        RestClient.getInstance().login(username, password, response -> {
            Platform.runLater(() -> {
                if (isLoginSuccess(response)) {
                    try {
                        loadHome(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    errorLabel.setText(extractErrorMessage(response));
                    errorLabel.setVisible(true);
                }
            });
        });
    }

    private void setPasswordVisibility(boolean visible) {
        if (visible) {
            passwordVisible.setVisible(true);
            passwordField.setVisible(false);
            eyeToggleBtn.setText("ðŸ™ˆ");
            return;
        }

        passwordVisible.setVisible(false);
        passwordField.setVisible(true);
        eyeToggleBtn.setText("ðŸ‘");
    }

    private static JsonObject parseResponseBody(String response) {
        if (response == null || response.isBlank()) {
            return null;
        }
        try {
            return JsonParser.parseString(response).getAsJsonObject();
        } catch (JsonSyntaxException | IllegalStateException | UnsupportedOperationException e) {
            return null;
        }
    }

    private static boolean isLoginSuccess(String response) {
        JsonObject root = parseResponseBody(response);
        if (root == null || !root.has("success") || root.get("success").isJsonNull()) {
            return false;
        }
        return root.get("success").getAsBoolean();
    }

    private static String extractErrorMessage(String response) {
        JsonObject root = parseResponseBody(response);
        if (root != null && root.has("message") && !root.get("message").isJsonNull()) {
            return root.get("message").getAsString();
        }
        return SERVER_UNREACHABLE_MESSAGE;
    }

    public void handleSignup(ActionEvent event) {
        try {
            SceneNavigator.switchScene(event, SIGNUP_VIEW);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(SIGNUP_VIEW_NOT_FOUND_MESSAGE);
        }
    }

    protected void loadHome(ActionEvent event) {
        try {
            FXMLLoader frameLoader = new FXMLLoader(getClass().getResource(HOME_FRAME_VIEW));
            Parent homeFrameRoot = frameLoader.load();

            HomeFrameController frameController = frameLoader.getController();

            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource(HOME_VIEW));
            Node homeNode = homeLoader.load();

            frameController.setView(homeNode);

            Stage stage = SceneNavigator.stageFromEvent(event);
            stage.setScene(new Scene(homeFrameRoot));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Lá»—i giao diá»‡n: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }
}

