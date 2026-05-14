package com.auction.client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.auction.client.service.AuctionService;
import com.auction.client.ClientContext;
import com.auction.share.exceptions.ValidationException;
import javafx.application.Platform;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class SellController implements Initializable {
    private final AuctionService auctionService = ClientContext.auctionService();

    // ──────────────── FXML Fields ────────────────
    @FXML private TextField name;
    @FXML private ComboBox<String> category;
    @FXML private TextField startingprice;
    @FXML private DatePicker enddate;
    @FXML private TextField description;
    @FXML private Button chooseImageButton;
    @FXML private Label imageLabel;
    @FXML private Button addButton;
    @FXML private Label validationLabel;
    @FXML private ImageView closeButton;

    // ──────────────── State ────────────────
    private File selectedImageFile;

    // ──────────────── Categories ────────────────
    private static final ObservableList<String> CATEGORIES = FXCollections.observableArrayList(
            "Antiques", "Art", "Books", "Clothing", "Collectibles",
            "Electronics", "Jewelry", "Music", "Sports", "Toys", "Other"
    );

    // ──────────────── Initialization ────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        category.setItems(CATEGORIES);
        validationLabel.setVisible(false);

        chooseImageButton.setOnAction(e -> handleChooseImage());
        addButton.setOnAction(e -> handleAddListing());
        closeButton.setOnMouseClicked(e -> handleClose());
    }

    // ──────────────── Handlers ────────────────

    /**
     * Opens a FileChooser for the user to select an image.
     */
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        Stage stage = (Stage) chooseImageButton.getScene().getWindow();
        selectedImageFile = fileChooser.showOpenDialog(stage);

        if (selectedImageFile != null) {
            imageLabel.setText(selectedImageFile.getName());
        } else {
            imageLabel.setText("No file selected");
        }
    }

    /**
     * Validates inputs and adds the listing.
     */
    private void handleAddListing() {
        String listingName      = name.getText().trim();
        String listingCategory  = category.getValue();
        String listingPrice     = startingprice.getText().trim();
        LocalDate listingDate   = enddate.getValue();
        String listingDesc      = description.getText().trim();

        String startTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String endTimeStr = listingDate != null ? listingDate.atTime(23, 59).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";

        try {
            auctionService.createAuction(
                    listingName, listingDesc, listingCategory, listingPrice, startTimeStr, endTimeStr,
                    response -> Platform.runLater(() -> {
                        if (response != null && response.isSuccess()) {
                            showSuccess("Listing added successfully!");
                            clearFields();
                        } else {
                            showError(response != null ? response.getMessage() : "Lỗi kết nối máy chủ");
                        }
                    })
            );
        } catch (ValidationException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Closes the current window.
     */
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    // ──────────────── Validation ────────────────

    private boolean validateInputs() {
        return true; // Delegation to Service layer
    }

    // ──────────────── Helpers ────────────────

    private void showError(String message) {
        validationLabel.setVisible(true);
        validationLabel.setStyle("-fx-text-fill: red;");
        validationLabel.setText(message);
    }

    private void showSuccess(String message) {
        validationLabel.setVisible(true);
        validationLabel.setStyle("-fx-text-fill: green;");
        validationLabel.setText(message);
    }

    private void clearFields() {
        name.clear();
        category.setValue(null);
        startingprice.clear();
        enddate.setValue(null);
        description.clear();
        imageLabel.setText("File Upload Label");
        selectedImageFile = null;
    }
}
