package com.auction.client.controller;

import com.auction.client.ClientContext;
import com.auction.client.service.UserService;
import com.auction.share.DTO.ProfileBidTransactionDTO;
import com.auction.share.DTO.ProfileDTO;
import com.auction.share.DTO.Response;
import com.auction.share.DTO.UserDTO;
import com.auction.share.exceptions.ValidationException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ProfileController implements Initializable {
  private UserService userService;

  public void setUserService(UserService userService) {
    this.userService = userService;
    loadProfileData();
  }

  @FXML private Label nameLabel;
  @FXML private TextField nameField;
  @FXML private TextField usernameField;
  @FXML private TextField emailField;
  @FXML private TextField phoneField;
  @FXML private PasswordField passwordField;
  @FXML private Button editButton;

  @FXML private TableView<ProfileBidTransactionDTO> table;
  @FXML private TableColumn<ProfileBidTransactionDTO, String> colItem;
  @FXML private TableColumn<ProfileBidTransactionDTO, String> colStatus;
  @FXML private TableColumn<ProfileBidTransactionDTO, Double> colBid;
  @FXML private TableColumn<ProfileBidTransactionDTO, String> colTime;

  private boolean isEditing = false;

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    setupTableColumns();
    editButton.setOnAction(event -> handleEditAction());
    setFieldsEditable(false);
  }

  private void setupTableColumns() {
    colItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));
    colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    colBid.setCellValueFactory(new PropertyValueFactory<>("bidAmount"));
    colTime.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

    colTime.setCellFactory(column -> new javafx.scene.control.TableCell<ProfileBidTransactionDTO, String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(com.auction.client.utils.DateTimeUtils.formatDateTimeForDisplay(item));
            }
        }
    });
  }

  public void loadProfileData() {
    userService.getProfile(response -> Platform.runLater(() -> bindProfileResponse(response)));
  }

  private void bindProfileResponse(Response<?> response) {
    if (response == null
        || !response.isSuccess()
        || !(response.getData() instanceof ProfileDTO profileDTO)) {
      return;
    }

    UserDTO user = profileDTO.getUser();
    if (user != null) {
      bindUser(user);
    }

    ObservableList<ProfileBidTransactionDTO> history =
        FXCollections.observableArrayList(profileDTO.getBidTransactions());
    table.setItems(history);
  }

  private void bindUser(UserDTO user) {
    nameLabel.setText(user.getFullName());
    nameField.setText(user.getFullName());
    usernameField.setText(user.getUsername());
    emailField.setText(user.getEmail());
    phoneField.setText(user.getPhoneNumber());
    passwordField.setText("");
  }

  private void setFieldsEditable(boolean canEdit) {
    nameField.setEditable(canEdit);
    emailField.setEditable(canEdit);
    phoneField.setEditable(canEdit);
    passwordField.setEditable(canEdit);
    usernameField.setEditable(false);

    String style =
        canEdit
            ? "-fx-background-color: white; -fx-border-color: #3498db;"
            : "-fx-background-color: #f0f0f0; -fx-border-color: transparent;";
    nameField.setStyle(style);
    emailField.setStyle(style);
    phoneField.setStyle(style);
    passwordField.setStyle(style);
    usernameField.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: transparent;");
  }

  private void saveProfileChanges() {
    try {
      userService.updateProfile(
          nameField.getText(),
          phoneField.getText(),
          emailField.getText(),
          passwordField.getText(),
          response ->
              Platform.runLater(
                  () -> {
                    if (response != null
                        && response.isSuccess()
                        && response.getData() instanceof UserDTO userDTO) {
                      bindUser(userDTO);
                      finishEditing();
                    } else {
                      showError(response != null ? response.getMessage() : "Unknown error");
                    }
                  }));
    } catch (ValidationException e) {
      showError(e.getMessage());
    }
  }

  private void showError(String message) {
    editButton.setText("Error: " + message);
    editButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
    javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
    pause.setOnFinished(e -> finishEditing());
    pause.play();
  }

  private void finishEditing() {
    isEditing = false;
    setFieldsEditable(false);
    editButton.setText("Edit Profile");
    editButton.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white;");
  }

  private void handleEditAction() {
    if (!isEditing) {
      isEditing = true;
      setFieldsEditable(true);
      editButton.setText("SAVE CHANGES");
      editButton.setStyle(
          "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
    } else {
      saveProfileChanges();
    }
  }
}
