package com.auction.client.controller;

import com.auction.client.ClientContext;
import com.auction.client.service.AuctionService;
import com.auction.share.exceptions.ValidationException;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.application.Platform;
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
import javafx.util.StringConverter;

public class SellController implements Initializable {
  private final AuctionService auctionService = ClientContext.auctionService();

  @FXML private TextField name;
  @FXML private ComboBox<String> category;
  @FXML private TextField startingprice;
  @FXML private DatePicker startdate;
  @FXML private ComboBox<String> startHour;
  @FXML private ComboBox<String> startMinute;
  @FXML private DatePicker enddate;
  @FXML private ComboBox<String> endHour;
  @FXML private ComboBox<String> endMinute;
  @FXML private TextField description;
  @FXML private Button chooseImageButton;
  @FXML private Label imageLabel;
  @FXML private Button addButton;
  @FXML private Label validationLabel;
  @FXML private ImageView closeButton;

  private File selectedImageFile;

  private static final ObservableList<String> CATEGORIES =
      FXCollections.observableArrayList(
          "Antique", "Art", "Electronic", "Jewelry", "RealEstate", "Vehicle");

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    category.setItems(CATEGORIES);
    validationLabel.setVisible(false);

    // Populate Hour and Minute ComboBoxes
    ObservableList<String> hours = FXCollections.observableArrayList();
    for (int i = 0; i <= 23; i++) hours.add(String.format("%02d", i));
    endHour.setItems(hours);
    endHour.setValue("23");
    startHour.setItems(hours);
    startHour.setValue(String.format("%02d", LocalDateTime.now().getHour()));

    ObservableList<String> minutes = FXCollections.observableArrayList();
    for (int i = 0; i <= 59; i++) minutes.add(String.format("%02d", i));
    endMinute.setItems(minutes);
    endMinute.setValue("59");
    startMinute.setItems(minutes);
    startMinute.setValue(String.format("%02d", LocalDateTime.now().getMinute()));

    StringConverter<LocalDate> dateConverter =
        new StringConverter<>() {
          final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

          @Override
          public String toString(LocalDate date) {
            if (date != null) {
              return displayFormatter.format(date);
            } else {
              return "";
            }
          }

          @Override
          public LocalDate fromString(String string) {
            if (string != null && !string.isEmpty()) {
              try {
                if (string.contains(" ")) {
                  String datePart = string.split(" ")[1];
                  return LocalDate.parse(datePart, displayFormatter);
                }
                if (string.contains("T")) {
                  return LocalDateTime.parse(string, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                      .toLocalDate();
                }
                return LocalDate.parse(string, displayFormatter);
              } catch (Exception e) {
                return null;
              }
            }
            return null;
          }
        };

    enddate.setConverter(
        new StringConverter<>() {
          @Override
          public String toString(LocalDate date) {
            if (date != null) {
              String hh = endHour.getValue() != null ? endHour.getValue() : "23";
              String mm = endMinute.getValue() != null ? endMinute.getValue() : "59";
              return hh + ":" + mm + " " + dateConverter.toString(date);
            }
            return "";
          }

          @Override
          public LocalDate fromString(String string) {
            return dateConverter.fromString(string);
          }
        });

    startdate.setConverter(
        new StringConverter<>() {
          @Override
          public String toString(LocalDate date) {
            if (date != null) {
              String hh = startHour.getValue() != null ? startHour.getValue() : "00";
              String mm = startMinute.getValue() != null ? startMinute.getValue() : "00";
              return hh + ":" + mm + " " + dateConverter.toString(date);
            }
            return "";
          }

          @Override
          public LocalDate fromString(String string) {
            return dateConverter.fromString(string);
          }
        });

    // Add listeners to update DatePicker text when time changes
    endHour.valueProperty().addListener((obs, oldVal, newVal) -> updateDatePickerText(enddate));
    endMinute.valueProperty().addListener((obs, oldVal, newVal) -> updateDatePickerText(enddate));
    startHour.valueProperty().addListener((obs, oldVal, newVal) -> updateDatePickerText(startdate));
    startMinute
        .valueProperty()
        .addListener((obs, oldVal, newVal) -> updateDatePickerText(startdate));

    startdate.setValue(LocalDate.now()); // Set default start date to today

    chooseImageButton.setOnAction(e -> handleChooseImage());
    addButton.setOnAction(e -> handleAddListing());
    closeButton.setOnMouseClicked(e -> handleClose());
  }

  private void updateDatePickerText(DatePicker picker) {
    if (picker.getValue() != null) {
      picker.getEditor().setText(picker.getConverter().toString(picker.getValue()));
    }
  }

  private boolean validateInputs() {
    return !name.getText().trim().isEmpty()
        && category.getValue() != null
        && !startingprice.getText().trim().isEmpty()
        && enddate.getValue() != null
        && startdate.getValue() != null;
  }

  private void showError(String message) {
    validationLabel.setVisible(true);
    validationLabel.setStyle("-fx-text-fill: red;");
    validationLabel.setText(message);
  }

  private void showSuccess() {
    validationLabel.setVisible(true);
    validationLabel.setStyle("-fx-text-fill: green;");
    validationLabel.setText("Listing added successfully!");
  }

  private void clearFields() {
    name.clear();
    category.setValue(null);
    startingprice.clear();
    startdate.setValue(LocalDate.now());
    enddate.setValue(null);
    startHour.setValue(String.format("%02d", LocalDateTime.now().getHour()));
    startMinute.setValue(String.format("%02d", LocalDateTime.now().getMinute()));
    endHour.setValue("23");
    endMinute.setValue("59");
    description.clear();
    imageLabel.setText("File Upload Label");
    selectedImageFile = null;
  }

  private void handleChooseImage() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select Image");
    fileChooser
        .getExtensionFilters()
        .addAll(
            new FileChooser.ExtensionFilter(
                "Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));

    Stage stage = (Stage) chooseImageButton.getScene().getWindow();
    selectedImageFile = fileChooser.showOpenDialog(stage);

    if (selectedImageFile != null) {
      imageLabel.setText(selectedImageFile.getName());
    } else {
      imageLabel.setText("No file selected");
    }
  }

  private void handleAddListing() {
    submitAuction();
  }

  private void submitAuction() {
    String listingName = name.getText().trim();
    String listingCategory = category.getValue();
    String listingPrice = startingprice.getText().trim();
    LocalDate endDate = enddate.getValue();
    LocalDate startDate = startdate.getValue();
    String listingDesc = description.getText().trim();

    if (!validateInputs()) {
      showError("Please fill out the entire form.");
      return;
    }

    String eHH = endHour.getValue() != null ? endHour.getValue() : "23";
    String emm = endMinute.getValue() != null ? endMinute.getValue() : "59";
    String sHH = startHour.getValue() != null ? startHour.getValue() : "00";
    String smm = startMinute.getValue() != null ? startMinute.getValue() : "00";

    String startTimeStr =
        startDate
            .atTime(Integer.parseInt(sHH), Integer.parseInt(smm))
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    String endTimeStr =
        endDate
            .atTime(Integer.parseInt(eHH), Integer.parseInt(emm))
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    byte[] imageBytes = null;
    String imageName = null;

    if (selectedImageFile != null) {
      if (selectedImageFile.length() > 5 * 1024 * 1024) {
        showError("Kích thước ảnh vượt quá giới hạn cho phép (tối đa 5MB)!");
        return;
      }
      try {
        imageBytes = java.nio.file.Files.readAllBytes(selectedImageFile.toPath());
        imageName = selectedImageFile.getName();
      } catch (java.io.IOException e) {
        showError("Không thể đọc tệp hình ảnh đã chọn!");
        return;
      }
    }

    try {
      auctionService.createAuction(
          listingName,
          listingDesc,
          listingCategory,
          listingPrice,
          startTimeStr,
          endTimeStr,
          imageBytes,
          imageName,
          response ->
              Platform.runLater(
                  () -> {
                    if (response != null && response.isSuccess()) {
                      showSuccess();
                      clearFields();
                    } else {
                      showError(
                          response != null ? response.getMessage() : "Server connection failed.");
                    }
                  }));
    } catch (ValidationException e) {
      showError(e.getMessage());
    }
  }

  private void handleClose() {
    Stage stage = (Stage) closeButton.getScene().getWindow();
    stage.close();
  }
}
