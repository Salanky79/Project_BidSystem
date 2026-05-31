package com.auction.client.controller;

import com.auction.client.ClientContext;
import com.auction.client.factory.DashboardNavigator;
import com.auction.client.service.UserService;
import com.auction.share.DTO.LoginRequest;
import com.auction.share.DTO.Response;
import com.auction.share.DTO.UserDTO;
import com.auction.share.exceptions.ValidationException;
import java.io.IOException;
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

public class LoginController {
  private UserService userService;

  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  @FXML private TextField usernameField;

  @FXML private PasswordField passwordField;

  @FXML private Label errorLabel;

  @FXML private TextField passwordVisible;

  @FXML private Button eyeToggleBtn;

  private boolean isPasswordVisible = false;

  @FXML
  // ham tu chay moi khi khoi tao
  public void initialize() {
    // lien ket giua hai chieu
    passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());
    passwordVisible.setVisible(false);
    passwordField.setVisible(true);
  }

  // o hien thi hoac an password
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

  // kiem tra message neu co
  private static String extractErrorMessage(Response<?> response) {
    if (response != null && response.getMessage() != null && !response.getMessage().isBlank()) {
      return response.getMessage();
    }
    return "Khong the ket noi Server!";
  }

  protected void loadDashboard(ActionEvent event, String role) {
    try {
      // lấy cửa sổ hiện tại
      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
      DashboardNavigator.openDashboard(stage, role);
    } catch (IOException e) {
      e.printStackTrace();
      errorLabel.setText("Loi giao dien: " + e.getMessage());
      errorLabel.setVisible(true);
    }
  }

  @FXML
  private void handleLogin(ActionEvent event) {
    String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
    String password = isPasswordVisible ? passwordVisible.getText() : passwordField.getText();

    try {
      // gọi userService
      // chạy Background Thread ( ko chạy trên JavaFX Application Thread => bị đơ)
      userService.login(
          new LoginRequest(username, password),
          response ->
              Platform.runLater(
                  () -> {
                    if (response.isSuccess()) {
                      try {
                        String role = null;
                        if (response.getData() instanceof UserDTO userDTO) {
                          role = userDTO.getRole();
                        }

                        // load màn hình login tương ứng
                        loadDashboard(event, role);
                      } catch (Exception e) {
                        e.printStackTrace();
                        errorLabel.setText("Failed to load screen: " + e.getMessage());
                        errorLabel.setVisible(true);
                      }
                    } else {
                      errorLabel.setText(extractErrorMessage(response));
                      errorLabel.setVisible(true);
                    }
                  }));
    } catch (ValidationException e) {
      errorLabel.setText(e.getMessage());
      errorLabel.setVisible(true);
    }
  }

  public void handleSignup(ActionEvent event) {
    try {
      FXMLLoader loader =
          new FXMLLoader(getClass().getResource("/com/auction/client/view/SignupView.fxml"));
      Parent loginRoot = loader.load();
      if (loader.getController() instanceof SignupController signupCtrl) {
          signupCtrl.setUserService(this.userService);
      }
      // tạo scene mới ( signup ) => gán vào stage hiện tại
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
}
