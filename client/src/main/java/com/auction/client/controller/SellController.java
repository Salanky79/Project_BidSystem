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
  private AuctionService auctionService;
  private Runnable onSuccessCallback;

  public void setAuctionService(AuctionService auctionService) {
      this.auctionService = auctionService;
  }

  public void setOnSuccessCallback(Runnable callback) {
    this.onSuccessCallback = callback;
  }

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
    if (name.getText().trim().isEmpty()) {
      showError("Tên sản phẩm không được để trống!");
      return false;
    }
    if (category.getValue() == null) {
      showError("Vui lòng chọn danh mục!");
      return false;
    }
    if (startingprice.getText().trim().isEmpty()) {
      showError("Vui lòng nhập giá khởi điểm!");
      return false;
    }
    try {
      double price = Double.parseDouble(startingprice.getText().trim());
      if (price <= 0) {
        showError("Giá khởi điểm phải lớn hơn 0!");
        return false;
      }
    } catch (NumberFormatException e) {
      showError("Giá khởi điểm không hợp lệ!");
      return false;
    }
    if (startdate.getValue() == null) {
      showError("Vui lòng chọn ngày bắt đầu!");
      return false;
    }
    if (enddate.getValue() == null) {
      showError("Vui lòng chọn ngày kết thúc!");
      return false;
    }
    return true;
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
                "Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.PNG", "*.JPG", "*.JPEG", "*.GIF", "*.BMP"),
            new FileChooser.ExtensionFilter("All Files", "*.*"));

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
    String listingPriceStr = startingprice.getText().trim();
    LocalDate endDate = enddate.getValue();
    LocalDate startDate = startdate.getValue();
    String listingDesc = description.getText().trim();

    if (!validateInputs()) {
      return;
    }

    double listingPrice;
    try {
      listingPrice = Double.parseDouble(listingPriceStr);
    } catch (NumberFormatException e) {
      showError("Giá khởi điểm không hợp lệ!");
      return;
    }
    String eHH = endHour.getValue() != null ? endHour.getValue() : "23";
    String emm = endMinute.getValue() != null ? endMinute.getValue() : "59";
    String sHH = startHour.getValue() != null ? startHour.getValue() : "00";
    String smm = startMinute.getValue() != null ? startMinute.getValue() : "00";

    LocalDateTime startTime = startDate.atTime(Integer.parseInt(sHH), Integer.parseInt(smm));
    LocalDateTime endTime = endDate.atTime(Integer.parseInt(eHH), Integer.parseInt(emm));

    // Disable button to prevent double-click or multiple submissions
    addButton.setDisable(true);
    addButton.setText("Adding...");

    if (selectedImageFile != null) {
      if (selectedImageFile.length() > 5 * 1024 * 1024) {
        showError("Kích thước ảnh vượt quá giới hạn cho phép (tối đa 5MB)!");
        addButton.setDisable(false);
        addButton.setText("Add");
        return;
      }
      java.util.concurrent.CompletableFuture.supplyAsync(() -> {
        try {
          return java.nio.file.Files.readAllBytes(selectedImageFile.toPath());
        } catch (java.io.IOException e) {
          throw new RuntimeException(e);
        }
      }).thenAcceptAsync(bytes -> {
        // C1: Chạy trên background thread (ForkJoinPool) — KHÔNG dùng Platform::runLater ở đây
        // để tránh socket I/O block JavaFX thread
        doSubmitAuction(listingName, listingDesc, listingCategory, listingPrice, startTime, endTime, bytes, selectedImageFile.getName());
      }).exceptionally(e -> {
        Platform.runLater(() -> {
          showError("Không thể đọc tệp hình ảnh đã chọn!");
          addButton.setDisable(false);
          addButton.setText("Add");
        });
        return null;
      });
    } else {
      doSubmitAuction(listingName, listingDesc, listingCategory, listingPrice, startTime, endTime, null, null);
    }
  }

  private void doSubmitAuction(String listingName, String listingDesc, String listingCategory, double listingPrice, LocalDateTime startTime, LocalDateTime endTime, byte[] imageBytes, String imageName) {
    try {
      auctionService.createAuction(listingName, listingDesc, listingCategory, listingPrice, startTime, endTime, imageBytes, imageName, response -> Platform.runLater(() -> {
        addButton.setDisable(false);
        addButton.setText("Add");
        if (response != null && response.isSuccess()) {
          showSuccess();
          clearFields();
          if (onSuccessCallback != null) {
              onSuccessCallback.run();
          }
        } else {
          showError(response != null ? response.getMessage() : "Server connection failed.");
        }
      }));
    } catch (ValidationException e) {
      Platform.runLater(() -> {
        addButton.setDisable(false);
        addButton.setText("Add");
        showError(e.getMessage());
      });
    }
  }

  private void handleClose() {
    Stage stage = (Stage) closeButton.getScene().getWindow();
    stage.close();
  }
}
