package com.auction.client.controller;

import com.auction.client.service.DeletedAuctionsStore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SellerItemCardController {

  @FXML private HBox cardRoot;
  @FXML private StackPane itemcard;
  @FXML private Label iconLabel;
  @FXML private Label categoryLabel;
  @FXML private Label nameLabel;
  @FXML private Label priceLabel;
  @FXML private Label bidsLabel;
  @FXML private Label timeLabel;
  @FXML private Label statusLabel;
  @FXML private Button deleteButton;

  private String icon;
  private String category;
  private String name;
  private String status;
  private double price;
  private int bids;
  private String time;
  private String auctionId;

  private static final DateTimeFormatter DISPLAY_FMT =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
  private static final DateTimeFormatter ISO_FMT =
      DateTimeFormatter.ISO_LOCAL_DATE_TIME;
  private static final DateTimeFormatter ALT_DB_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private Runnable refreshCallback;

  public void setRefreshCallback(Runnable refreshCallback) {
    this.refreshCallback = refreshCallback;
  }

  public void setData(
      String icon,
      String category,
      String name,
      double price,
      int bids,
      String time,
      String status,
      String auctionId) {
    setData(icon, category, name, price, bids, time, status, auctionId, null);
  }

  public void setData(
      String icon,
      String category,
      String name,
      double price,
      int bids,
      String time,
      String status,
      String auctionId,
      String imageUrl) {
    statusLabel.setText(status);
    categoryLabel.setText(category);
    nameLabel.setText(name);

    this.icon = icon;
    this.category = category;
    this.name = name;
    this.price = price;
    this.bids = bids;
    this.time = time;
    this.status = status;
    this.auctionId = auctionId;

    // Load ảnh sản phẩm từ Cloudinary
    if (imageUrl != null && !imageUrl.isBlank()) {
      iconLabel.setVisible(false);
      ImageView imageView = new ImageView();
      imageView.setFitWidth(160);
      imageView.setFitHeight(160);
      imageView.setPreserveRatio(false); // fill to fit card border
      
      // Load ảnh bất đồng bộ ngầm (background loading = true)
      Image image = new Image(imageUrl, 160, 160, false, true, true);
      imageView.setImage(image);
      
      // Clip ảnh bo tròn góc trên bên trái và dưới bên trái khớp với CSS StackPane
      javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(160, 160);
      clip.setArcWidth(24); // khớp với border-radius: 12 của StackPane
      clip.setArcHeight(24);
      imageView.setClip(clip);

      // Thêm imageView vào làm con đầu tiên của StackPane (sau statusLabel)
      if (itemcard != null) {
        // Xóa các ImageView cũ nếu có (để tránh chồng chéo khi render lại)
        itemcard.getChildren().removeIf(node -> node instanceof ImageView);
        itemcard.getChildren().add(0, imageView);
      }
    } else {
      iconLabel.setVisible(true);
      iconLabel.setText(icon);
      if (itemcard != null) {
        itemcard.getChildren().removeIf(node -> node instanceof ImageView);
      }
    }

    priceLabel.setText(String.format("%,.0f VND", price));

    bidsLabel.setText(bids + (bids <= 1 ? " bid" : " bids"));
    timeLabel.setText("Ending In : " + formatDateTimeForDisplay(time));

    // Fetch actual bid count asynchronously
    com.auction.client.ClientContext.auctionService().getAuctionDetail(this.auctionId, response -> {
      if (response != null && response.isSuccess() && response.getData() instanceof com.auction.share.DTO.AuctionDetailDTO detail) {
        int actualBids = detail.getBidHistory() != null ? detail.getBidHistory().size() : 0;
        this.bids = actualBids;
        javafx.application.Platform.runLater(() -> {
          if (bidsLabel != null) {
            bidsLabel.setText(actualBids + (actualBids <= 1 ? " bid" : " bids"));
          }
        });
      }
    });

    if (cardRoot != null) {
      cardRoot.setOnMouseClicked(event -> handleCardClick());
    }

    // ── Delete button: only visible for CANCELED auctions ──
    if (deleteButton != null) {
      if ("CANCELED".equals(status)) {
        deleteButton.setVisible(true);
        deleteButton.setManaged(true);
        deleteButton.setOnAction(
            event -> {
              event.consume(); // prevent click from bubbling to cardRoot
              handleDelete();
            });
      } else {
        deleteButton.setVisible(false);
        deleteButton.setManaged(false);
      }
    }

    if (status.equals("Active")) {
      statusLabel.setStyle(
          "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;"
              + " -fx-background-radius: 5px; -fx-padding: 2 5 2 5;");
    }
    if (status.equals("End")) {
      statusLabel.setStyle(
          "-fx-background-color: #FF3737; -fx-text-fill: white; -fx-font-weight: bold;"
              + " -fx-background-radius: 5px; -fx-padding: 2 5 2 5;");
    }
    if (status.equals("In Queue")) {
      statusLabel.setStyle(
          "-fx-background-color: #4C8CE4; -fx-text-fill: white; -fx-font-weight: bold;"
              + " -fx-background-radius: 5px; -fx-padding: 2 5 2 5;");
    }
    if (status.equals("CANCELED")) { // Keep matching original logic
      statusLabel.setStyle(
          "-fx-background-color: #605B51; -fx-text-fill: white; -fx-font-weight: bold;"
              + " -fx-background-radius: 5px; -fx-padding: 2 5 2 5;");
    }
  }

  private void handleDelete() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Delete Auction");
    alert.setHeaderText("Delete \"" + name + "\"?");
    alert.setContentText("This auction will be removed from the listing. This action cannot be undone during this session.");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      DeletedAuctionsStore.getInstance().delete(auctionId);
      if (refreshCallback != null) {
        refreshCallback.run();
      }
    }
  }

  @FXML
  private void handleCardClick() {
    SellerAuctionDetailController.open(icon, category, name, price, bids, time, status, auctionId);
  }

  private static LocalDateTime parseDateTime(String raw) {
    if (raw == null) return null;
    String s = raw.trim();
    if (s.isEmpty()) return null;

    try { return LocalDateTime.parse(s, ISO_FMT); } catch (Exception ignored) {}
    try { return LocalDateTime.parse(s, DISPLAY_FMT); } catch (Exception ignored) {}
    try { return LocalDateTime.parse(s, ALT_DB_FMT); } catch (Exception ignored) {}
    try { return LocalDateTime.parse(s.replace(' ', 'T'), ISO_FMT); } catch (Exception ignored) {}

    return null;
  }

  private static String formatDateTimeForDisplay(String raw) {
    LocalDateTime dt = parseDateTime(raw);
    if (dt == null) return raw == null ? "N/A" : raw;
    return dt.format(DISPLAY_FMT);
  }
}

