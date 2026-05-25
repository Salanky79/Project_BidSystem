package com.auction.client.controller;

import com.auction.client.ClientContext;
import com.auction.client.factory.DashboardNavigator;
import com.auction.client.service.UserService;
import com.auction.share.DTO.RegisterRequest;
import com.auction.share.exceptions.ValidationException;
import java.io.IOException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SignupController {
  private final UserService userService = ClientContext.userService();

  @FXML private TextField fullnameField;
  @FXML private TextField usernameField;
  @FXML private TextField emailField;
  @FXML private TextField phoneField;
  @FXML private TextField addressField;
  @FXML private PasswordField passwordField;
  @FXML private PasswordField confirmPasswordField;
  @FXML private Label fullnameError;
  @FXML private Label usernameError;
  @FXML private Label emailError;
  @FXML private Label phoneError;
  @FXML private Label addressError;
  @FXML private Label passwordError;
  @FXML private Label confirmError;
  @FXML private Label statusLabel;
  @FXML private VBox sellerCard;
  @FXML private VBox bidderCard;
  @FXML private ComboBox<String> roleCombo;

  private final String defaultStyle =
      "-fx-border-color:#cccccc; -fx-border-width:1.5; "
          + "-fx-border-radius:8; -fx-background-radius:8; "
          + "-fx-background-color:#ffffff; -fx-cursor:hand; -fx-padding:10;";

  private final String selectedStyle =
      "-fx-border-color:#3366cc; -fx-border-width:1.5; "
          + "-fx-border-radius:8; -fx-background-radius:8; "
          + "-fx-background-color:#e6f0ff; -fx-cursor:hand; -fx-padding:10;";

  private void resetErrors() {
    show(fullnameError, false);
    highlight(fullnameField, false);
    show(usernameError, false);
    highlight(usernameField, false);
    show(emailError, false);
    highlight(emailField, false);
    show(phoneError, false);
    highlight(phoneField, false);
    show(addressError, false);
    highlight(addressField, false);
    show(passwordError, false);
    highlight(passwordField, false);
    show(confirmError, false);
    highlight(confirmPasswordField, false);
  }

  private static String safeTrim(TextField field) {
    if (field == null || field.getText() == null) {
      return null;
    }
    return field.getText().trim();
  }

  private void show(Label label, boolean visible) {
    if (label == null) {
      return;
    }
    label.setVisible(visible);
    label.setManaged(visible);
  }

  private void highlight(Control ctrl, boolean error) {
    if (ctrl == null) {
      return;
    }
    String base =
        "-fx-background-color: #ffffff; -fx-border-width: 1; "
            + "-fx-border-radius: 6; -fx-background-radius: 6; "
            + "-fx-font-size: 14px; -fx-padding: 10 14 10 14; -fx-pref-height: 42;";
    if (error) {
      ctrl.setStyle(base + "-fx-border-color: #cc3333;");
    } else {
      ctrl.setStyle(base + "-fx-border-color: #cccccc;");
    }
  }

  private void showValidationError(ValidationException e) {
    statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #cc3333;");
    statusLabel.setText(e.getMessage());

    String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
    if (msg.contains("ho ten") || msg.contains("full name")) {
      show(fullnameError, true);
      highlight(fullnameField, true);
    } else if (msg.contains("username")) {
      show(usernameError, true);
      highlight(usernameField, true);
    } else if (msg.contains("email")) {
      show(emailError, true);
      highlight(emailField, true);
    } else if (msg.contains("dien thoai") || msg.contains("phone")) {
      show(phoneError, true);
      highlight(phoneField, true);
    } else if (msg.contains("dia chi") || msg.contains("address")) {
      show(addressError, true);
      highlight(addressField, true);
    } else if (msg.contains("password") || msg.contains("mat khau")) {
      show(passwordError, true);
      highlight(passwordField, true);
      show(confirmError, true);
      highlight(confirmPasswordField, true);
    }
  }

  private void openDashboard(String role, String fullName) {
    statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #228B22;");
    statusLabel.setText("Dang ky thanh cong! Chao mung, " + fullName);

    try {
      Stage stage = (Stage) statusLabel.getScene().getWindow();
      DashboardNavigator.openDashboard(stage, role);
    } catch (IOException e) {
      e.printStackTrace();
      statusLabel.setText("Loi khi chuyen man hinh!");
      statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #cc3333;");
    }
  }

  @FXML
  private void handleSignup() {
    resetErrors();

    String fullName = safeTrim(fullnameField);
    String username = safeTrim(usernameField);
    String email = safeTrim(emailField);
    String phoneNumber = safeTrim(phoneField);
    String address = safeTrim(addressField);
    String password = passwordField != null ? passwordField.getText() : null;
    String confirmPassword = confirmPasswordField != null ? confirmPasswordField.getText() : null;
    String role = roleCombo != null ? roleCombo.getValue() : null;

    try {
      if (confirmPassword == null || !confirmPassword.equals(password)) {
        throw new ValidationException("Mat khau xac nhan khong khop!");
      }

      RegisterRequest request =
          new RegisterRequest(username, password, fullName, role, phoneNumber, email, address);
      userService.signup(
          request,
          response ->
              Platform.runLater(
                  () -> {
                    if (response.isSuccess()) {
                      openDashboard(role, fullName);
                    } else {
                      statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #cc3333;");
                      statusLabel.setText(response.getMessage());
                    }
                  }));
    } catch (ValidationException e) {
      showValidationError(e);
    }
  }

  public void handleLogin(ActionEvent event) {
    try {
      FXMLLoader loader =
          new FXMLLoader(getClass().getResource("/com/auction/client/view/Login.fxml"));
      Parent loginRoot = loader.load();

      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
      stage.setScene(new Scene(loginRoot));
      stage.centerOnScreen();
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void selectSeller() {
    if (sellerCard != null) sellerCard.setStyle(selectedStyle);
    if (bidderCard != null) bidderCard.setStyle(defaultStyle);
    if (roleCombo != null) roleCombo.setValue("Seller");
  }

  public void selectBidder() {
    if (bidderCard != null) bidderCard.setStyle(selectedStyle);
    if (sellerCard != null) sellerCard.setStyle(defaultStyle);
    if (roleCombo != null) roleCombo.setValue("Bidder");
  }
}
