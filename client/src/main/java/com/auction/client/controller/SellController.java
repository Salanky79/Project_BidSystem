package com.auction.client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class SellController implements Initializable {
    private static final String DEFAULT_IMAGE_LABEL = "File Upload Label";
    private static final String NO_FILE_SELECTED_LABEL = "No file selected";
    private static final String ERROR_LABEL_STYLE = "-fx-text-fill: red;";
    private static final String SUCCESS_LABEL_STYLE = "-fx-text-fill: green;";
    private static final ObservableList<String> CATEGORIES = FXCollections.observableArrayList(
            "Antiques", "Art", "Books", "Clothing", "Collectibles",
            "Electronics", "Jewelry", "Music", "Sports", "Toys", "Other"
    );

    @FXML
    private TextField name;
    @FXML
    private ComboBox<String> category;
    @FXML
    private TextField startingprice;
    @FXML
    private DatePicker enddate;
    @FXML
    private TextField description;
    @FXML
    private Button chooseImageButton;
    @FXML
    private Label imageLabel;
    @FXML
    private Button addButton;
    @FXML
    private Label validationLabel;
    @FXML
    private ImageView closeButton;

    private File selectedImageFile;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        category.setItems(CATEGORIES);
        validationLabel.setVisible(false);

        chooseImageButton.setOnAction(e -> handleChooseImage());
        addButton.setOnAction(e -> handleAddListing());
        closeButton.setOnMouseClicked(e -> handleClose());
    }

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
            imageLabel.setText(NO_FILE_SELECTED_LABEL);
        }
    }

    private void handleAddListing() {
        if (!validateInputs()) {
            return;
        }

        String listingName = name.getText().trim();
        String listingCategory = category.getValue();
        double listingPrice = Double.parseDouble(startingprice.getText().trim());
        LocalDate listingDate = enddate.getValue();
        String listingDesc = description.getText().trim();

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

    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private boolean validateInputs() {
        if (name.getText().trim().isEmpty()) {
            showError("Please enter a title.");
            return false;
        }

        if (category.getValue() == null) {
            showError("Please select a category.");
            return false;
        }

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

        if (enddate.getValue() == null) {
            showError("Please select an end date.");
            return false;
        }
        if (!enddate.getValue().isAfter(LocalDate.now())) {
            showError("End date must be a future date.");
            return false;
        }

        if (description.getText().trim().isEmpty()) {
            showError("Please enter a description.");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        validationLabel.setVisible(true);
        validationLabel.setStyle(ERROR_LABEL_STYLE);
        validationLabel.setText(message);
    }

    private void showSuccess(String message) {
        validationLabel.setVisible(true);
        validationLabel.setStyle(SUCCESS_LABEL_STYLE);
        validationLabel.setText(message);
    }

    private void clearFields() {
        name.clear();
        category.setValue(null);
        startingprice.clear();
        enddate.setValue(null);
        description.clear();
        imageLabel.setText(DEFAULT_IMAGE_LABEL);
        selectedImageFile = null;
    }
}

