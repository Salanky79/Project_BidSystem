package com.auction.client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class SellController implements Initializable {

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
        if (!validateInputs()) return;

        String listingName      = name.getText().trim();
        String listingCategory  = category.getValue();
        double listingPrice     = Double.parseDouble(startingprice.getText().trim());
        LocalDate listingDate   = enddate.getValue();
        String listingDesc      = description.getText().trim();

        // TODO: Replace with your actual database / service call
        System.out.println("=== New Listing ===");
        System.out.println("Title       : " + listingName);
        System.out.println("Category    : " + listingCategory);
        System.out.println("Start Price : " + listingPrice);
        System.out.println("End Date    : " + listingDate);
        System.out.println("Description : " + listingDesc);
        System.out.println("Image       : " + (selectedImageFile != null ? selectedImageFile.getAbsolutePath() : "None"));

        showSuccess("Listing added successfully!");
        clearFields();
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
        // Title
        if (name.getText().trim().isEmpty()) {
            showError("Please enter a title.");
            return false;
        }

        // Category
        if (category.getValue() == null) {
            showError("Please select a category.");
            return false;
        }

        // Starting Price – must be a positive number
        String priceText = startingprice.getText().trim();
        if (priceText.isEmpty()) {
            showError("Please enter a starting price.");
            return false;
        }
        try {
            double price = Double.parseDouble(priceText);
            if (price <= 0) {
                showError("Starting price must be greater than 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Starting price must be a valid number.");
            return false;
        }

        // End Date – must be in the future
        if (enddate.getValue() == null) {
            showError("Please select an end date.");
            return false;
        }
        if (!enddate.getValue().isAfter(LocalDate.now())) {
            showError("End date must be a future date.");
            return false;
        }

        // Description
        if (description.getText().trim().isEmpty()) {
            showError("Please enter a description.");
            return false;
        }

        return true;
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
