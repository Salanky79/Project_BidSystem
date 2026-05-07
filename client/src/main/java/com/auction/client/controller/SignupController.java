package com.auction.client.controller;

import com.auction.client.factory.BidderUIFactory;
import com.auction.client.factory.DashboardProduct;
import com.auction.client.factory.RoleUIFactory;
import com.auction.client.factory.SellerUIFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class SignupController {
    private static final String LOGIN_VIEW = "/com/auction/client/view/Login.fxml";
    private static final String STATUS_SUCCESS_STYLE = "-fx-font-size: 12px; -fx-text-fill: #228B22;";
    private static final String STATUS_ERROR_STYLE = "-fx-font-size: 12px; -fx-text-fill: #cc3333;";
    private static final String DEFAULT_CARD_STYLE = "-fx-border-color:#cccccc; -fx-border-width:1.5; " +
            "-fx-border-radius:8; -fx-background-radius:8; " +
            "-fx-background-color:#ffffff; -fx-cursor:hand; -fx-padding:10;";
    private static final String SELECTED_CARD_STYLE = "-fx-border-color:#3366cc; -fx-border-width:1.5; " +
            "-fx-border-radius:8; -fx-background-radius:8; " +
            "-fx-background-color:#e6f0ff; -fx-cursor:hand; -fx-padding:10;";

    @FXML
    private TextField fullnameField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label fullnameError;
    @FXML
    private Label usernameError;
    @FXML
    private Label passwordError;
    @FXML
    private Label confirmError;
    @FXML
    private Label statusLabel;

    @FXML
    private VBox sellerCard;
    @FXML
    private VBox bidderCard;
    @FXML
    private ComboBox<String> roleCombo;

    @FXML
    private void handleSignup() {
        boolean valid = validateInputs();

        if (valid) {
            statusLabel.setStyle(STATUS_SUCCESS_STYLE);
            statusLabel.setText("ÄÄƒng kÃ½ thÃ nh cÃ´ng! ChÃ o má»«ng, "
                    + fullnameField.getText().trim() + " ðŸŽ‰");

            RoleUIFactory factory = "Seller".equals(roleCombo.getValue())
                    ? new SellerUIFactory()
                    : new BidderUIFactory();

            try {
                DashboardProduct dashboard = factory.createDashboard();
                Scene scene = dashboard.getScene();
                Stage stage = (Stage) statusLabel.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle(dashboard.getTitle());
                stage.centerOnScreen();
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                statusLabel.setText("Lá»—i khi chuyá»ƒn mÃ n hÃ¬nh!");
                statusLabel.setStyle(STATUS_ERROR_STYLE);
            }
        } else {
            statusLabel.setStyle(STATUS_ERROR_STYLE);
            statusLabel.setText("Vui lÃ²ng Ä‘iá»n Ä‘áº§y Ä‘á»§ cÃ¡c trÆ°á»ng báº¯t buá»™c.");
        }
    }

    private boolean validateInputs() {
        boolean valid = true;

        if (fullnameField.getText().trim().isEmpty()) {
            show(fullnameError, true);
            highlight(fullnameField, true);
            valid = false;
        } else {
            show(fullnameError, false);
            highlight(fullnameField, false);
        }

        if (usernameField.getText().trim().isEmpty()) {
            show(usernameError, true);
            highlight(usernameField, true);
            valid = false;
        } else {
            show(usernameError, false);
            highlight(usernameField, false);
        }

        if (passwordField.getText().isEmpty()) {
            show(passwordError, true);
            highlight(passwordField, true);
            valid = false;
        } else {
            show(passwordError, false);
            highlight(passwordField, false);
        }

        if (!confirmPasswordField.getText().equals(passwordField.getText())
                || confirmPasswordField.getText().isEmpty()) {
            show(confirmError, true);
            highlight(confirmPasswordField, true);
            valid = false;
        } else {
            show(confirmError, false);
            highlight(confirmPasswordField, false);
        }
        return valid;
    }

    private void show(Label label, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
    }

    private void highlight(Control ctrl, boolean error) {
        String base = "-fx-background-color: #ffffff; -fx-border-width: 1; "
                + "-fx-border-radius: 6; -fx-background-radius: 6; "
                + "-fx-font-size: 14px; -fx-padding: 10 14 10 14; -fx-pref-height: 42;";
        if (error) {
            ctrl.setStyle(base + "-fx-border-color: #cc3333;");
            return;
        }
        ctrl.setStyle(base + "-fx-border-color: #cccccc;");
    }

    public void handleLogin(ActionEvent event) {
        System.out.println("Trá»Ÿ vá» mÃ n hÃ¬nh Login!");
        try {
            SceneNavigator.switchScene(event, LOGIN_VIEW);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lá»—i: KhÃ´ng tÃ¬m tháº¥y file Login.fxml!");
        }
    }

    public void selectSeller(MouseEvent mouseEvent) {
        sellerCard.setStyle(SELECTED_CARD_STYLE);
        bidderCard.setStyle(DEFAULT_CARD_STYLE);
        if (roleCombo != null) {
            roleCombo.setValue("Seller");
        }
    }

    public void selectBidder(MouseEvent mouseEvent) {
        bidderCard.setStyle(SELECTED_CARD_STYLE);
        sellerCard.setStyle(DEFAULT_CARD_STYLE);
        if (roleCombo != null) {
            roleCombo.setValue("Bidder");
        }
    }
}

