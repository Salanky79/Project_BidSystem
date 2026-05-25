package com.auction.client.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SellerAuctionDetailController {

  // ── HEADER ──────────────────────────────────────────────────
  @FXML private Button closeButton;
  @FXML private Label productTitleLabel;

  // ── CHART ───────────────────────────────────────────────────
  @FXML private Button btnDay;
  @FXML private Button btnWeek;
  @FXML private Button btnMonth;
  @FXML private LineChart<String, Number> priceHistoryChart;

  // ── STATS ───────────────────────────────────────────────────
  @FXML private Label currentHighBidLabel;
  @FXML private Label totalBidsLabel;
  @FXML private Label startTimeLabel;
  @FXML private Label endTimeLabel;
  @FXML private Label endsInLabel;

  // ── CONTROLS ────────────────────────────────────────────────
  @FXML private Button pauseAuctionButton;
  @FXML private Button cancelAuctionButton;
  @FXML private TextField bidStepField;
  @FXML private Button saveBidStepButton;
  @FXML private TextField extendEndTimeField;
  @FXML private Button saveExtendTimeButton;

  // ── BIDDERS ─────────────────────────────────────────────────
  @FXML private ListView<String> bidderListView;

  // ── COMMENTS ────────────────────────────────────────────────
  @FXML private ListView<String> commentsListView;
  @FXML private TextField commentInputField;
  @FXML private Button postCommentButton;

  private String auctionId;
  private double currentPrice;
  private double startingPrice = 0;
  private LocalDateTime endTime;
  private Timeline countdownTimeline;
  private boolean isPaused = false;
  private static final int MAX_CHART_POINTS = 5;
  // Persistent series – never replaced, only data points are added/removed
  private final XYChart.Series<String, Number> chartSeries = new XYChart.Series<>();

  private static final DateTimeFormatter DISPLAY_FMT =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
  private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  @FXML
  public void initialize() {
    if (closeButton != null) {
      closeButton.setOnAction(
          e -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
          });
    }

    // Chart filters
    if (btnDay != null) btnDay.setOnAction(e -> loadChartData("day"));
    if (btnWeek != null) btnWeek.setOnAction(e -> loadChartData("week"));
    if (btnMonth != null) btnMonth.setOnAction(e -> loadChartData("month"));
    if (priceHistoryChart != null) {
      NumberAxis yAxis = (NumberAxis) priceHistoryChart.getYAxis();
      yAxis.setAutoRanging(true);
      yAxis.setForceZeroInRange(false);
      priceHistoryChart.getData().add(chartSeries);
      // will be populated after setData() sets currentPrice
    }

    // Action Buttons
    if (pauseAuctionButton != null) {
      pauseAuctionButton.setOnAction(
          e -> {
            isPaused = !isPaused;
            if (isPaused) {
              pauseAuctionButton.setText("▶  Resume Auction");
              pauseAuctionButton.setStyle(
                  "-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;"
                      + " -fx-font-size: 12px; -fx-padding: 10 12 10 12; -fx-background-radius: 6;"
                      + " -fx-border-radius: 6;");
            } else {
              pauseAuctionButton.setText("⏸  Pause Auction");
              pauseAuctionButton.setStyle(
                  "-fx-background-color: transparent; -fx-border-color: #B32626; -fx-border-width:"
                      + " 1; -fx-text-fill: #E57373; -fx-font-weight: bold; -fx-font-size: 12px;"
                      + " -fx-padding: 10 12 10 12; -fx-background-radius: 6; -fx-border-radius:"
                      + " 6;");
            }
          });
    }

    if (cancelAuctionButton != null) {
      cancelAuctionButton.setOnAction(
          e -> {
            Alert alert =
                new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to cancel this auction?",
                    ButtonType.YES,
                    ButtonType.NO);
            alert
                .showAndWait()
                .ifPresent(
                    res -> {
                      if (res == ButtonType.YES) {
                        com.auction.client.ClientContext.auctionService()
                            .cancelAuction(
                                auctionId,
                                response ->
                                    Platform.runLater(
                                        () -> {
                                          if (response != null && response.isSuccess()) {
                                            cancelAuctionButton.setText("Cancelled");
                                            cancelAuctionButton.setDisable(true);
                                            if (pauseAuctionButton != null)
                                              pauseAuctionButton.setDisable(true);
                                            if (endsInLabel != null)
                                              endsInLabel.setText("Cancelled");
                                            if (countdownTimeline != null) countdownTimeline.stop();
                                          } else {
                                            Alert error =
                                                new Alert(
                                                    Alert.AlertType.ERROR,
                                                    "Failed to cancel auction: "
                                                        + (response != null
                                                            ? response.getMessage()
                                                            : "Unknown error"));
                                            error.show();
                                          }
                                        }));
                      }
                    });
          });
    }

    // Post Comment
    if (postCommentButton != null) {
      postCommentButton.setOnAction(e -> handlePostComment());
    }

    if (saveBidStepButton != null) {
      saveBidStepButton.setOnAction(e -> handleSetBidStep());
    }

    if (saveExtendTimeButton != null) {
      saveExtendTimeButton.setOnAction(e -> handleExtendEndTime());
    }

    setupBidderList();
    setupCommentList();
  }

  private void setupBidderList() {
    if (bidderListView == null) return;

    bidderListView
        .getItems()
        .addAll(
            "User A|4.5|Contacted",
            "User B|3.0|Flagged",
            "User C|4.8|Pending",
            "User D|4.2|Confirmed");

    bidderListView.setCellFactory(
        lv ->
            new ListCell<>() {
              @Override
              protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setGraphic(null);
                  setStyle("-fx-background-color: transparent;");
                  return;
                }

                String[] parts = item.split("\\|", 3);
                String name = parts.length > 0 ? parts[0] : "Unknown";
                String rating = parts.length > 1 ? parts[1] : "0.0";
                String status = parts.length > 2 ? parts[2] : "Pending";

                Label iconLabel = new Label("👤");
                iconLabel.setStyle("-fx-text-fill: #D4AF37; -fx-font-size: 16px;");

                Label nameLabel = new Label(name + " (" + rating + "★)");
                nameLabel.setStyle(
                    "-fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-font-weight: bold;");

                Label statusLabel = new Label(status);
                String statusColor =
                    status.equals("Flagged")
                        ? "#E57373"
                        : (status.equals("Pending") ? "#FFD700" : "#4CAF50");
                statusLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 10px;");

                VBox infoBox = new VBox(1, nameLabel, statusLabel);
                HBox.setHgrow(infoBox, Priority.ALWAYS);

                Button profileBtn = new Button("👁 Profile");
                profileBtn.setStyle(
                    "-fx-background-color: transparent; -fx-border-color: #D4AF37;"
                        + " -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;"
                        + " -fx-text-fill: #D4AF37; -fx-font-size: 10px; -fx-padding: 4 8 4 8;"
                        + " -fx-cursor: hand;");

                Button banBtn = new Button("Ban");
                banBtn.setStyle(
                    "-fx-background-color: #B32626; -fx-border-color: #B32626; -fx-border-radius:"
                        + " 4; -fx-background-radius: 4; -fx-text-fill: #FFFFFF; -fx-font-size:"
                        + " 10px; -fx-font-weight: bold; -fx-padding: 4 8 4 8; -fx-cursor: hand;");

                HBox row = new HBox(8, iconLabel, infoBox, profileBtn, banBtn);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle(
                    "-fx-background-color: #1A1A1A; -fx-padding: 8 10 8 10; -fx-background-radius:"
                        + " 6;");

                setGraphic(row);
                setStyle("-fx-background-color: transparent; -fx-padding: 4 0 4 0;");
              }
            });
  }

  private void setupCommentList() {
    if (commentsListView == null) return;

    commentsListView
        .getItems()
        .addAll(
            "User B|When does it ship?|12/04/2026 11:30",
            "User A|Is it authentic?|12/04/2026 10:15");

    commentsListView.setCellFactory(
        lv ->
            new ListCell<>() {
              @Override
              protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setGraphic(null);
                  setStyle("-fx-background-color: transparent;");
                  return;
                }

                String[] parts = item.split("\\|", 3);
                String author = parts.length > 0 ? parts[0] : "?";
                String text = parts.length > 1 ? parts[1] : item;
                String timestamp = parts.length > 2 ? parts[2] : "";

                String initials =
                    author.length() >= 2
                        ? (author.charAt(0) + author.substring(author.length() - 1)).toUpperCase()
                        : author.toUpperCase();

                Label avatarLabel = new Label(initials);
                avatarLabel.setStyle(
                    "-fx-text-fill: #000000; -fx-font-weight: bold; -fx-font-size: 12px;");
                VBox avatar = new VBox(avatarLabel);
                avatar.setAlignment(Pos.CENTER);
                avatar.setStyle(
                    "-fx-min-width: 36; -fx-min-height: 36; -fx-max-width: 36; -fx-max-height: 36;"
                        + " -fx-background-color: #D4AF37; -fx-background-radius: 50;");

                Label authorLabel = new Label(author);
                authorLabel.setStyle(
                    "-fx-text-fill: #D4AF37; -fx-font-weight: bold; -fx-font-size: 13px;");
                HBox.setHgrow(authorLabel, Priority.ALWAYS);

                Label timeLabel = new Label(timestamp);
                timeLabel.setStyle("-fx-text-fill: #777777; -fx-font-size: 11px;");

                HBox header = new HBox(8, authorLabel, timeLabel);
                header.setAlignment(Pos.CENTER_LEFT);

                Label textLabel = new Label(text);
                textLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 13px;");
                textLabel.setWrapText(true);

                VBox content = new VBox(3, header, textLabel);
                HBox.setHgrow(content, Priority.ALWAYS);

                HBox card = new HBox(12, avatar, content);
                card.setAlignment(Pos.TOP_LEFT);
                card.setPadding(new Insets(12, 14, 12, 14));
                card.setStyle(
                    "-fx-background-color: #161616; -fx-background-radius: 8; -fx-border-color:"
                        + " #D4AF37 transparent transparent transparent; -fx-border-width: 0 0 0"
                        + " 3;");

                setGraphic(card);
                setStyle("-fx-background-color: transparent; -fx-padding: 4 0 4 0;");
                setPrefWidth(0);
              }
            });
  }

  private void handlePostComment() {
    if (commentInputField == null || commentsListView == null) return;
    String text = commentInputField.getText().trim();
    if (text.isEmpty()) return;

    String timestamp = LocalDateTime.now().format(DISPLAY_FMT);
    commentsListView.getItems().add(0, "Seller|" + text + "|" + timestamp);
    commentsListView.scrollTo(0);
    commentInputField.clear();
  }

  private void handleSetBidStep() {
    if (bidStepField == null || bidStepField.getText().trim().isEmpty()) return;
    try {
      double step = Double.parseDouble(bidStepField.getText().trim());
      com.auction.client.ClientContext.auctionService()
          .setBidStep(
              auctionId,
              step,
              response ->
                  Platform.runLater(
                      () -> {
                        if (response != null && response.isSuccess()) {
                          Alert success =
                              new Alert(Alert.AlertType.INFORMATION, "Bid step updated to " + step);
                          success.show();
                          bidStepField.clear();
                        } else {
                          Alert error =
                              new Alert(
                                  Alert.AlertType.ERROR,
                                  "Failed to update bid step: "
                                      + (response != null
                                          ? response.getMessage()
                                          : "Unknown error"));
                          error.show();
                        }
                      }));
    } catch (NumberFormatException e) {
      new Alert(Alert.AlertType.ERROR, "Invalid bid step value!").show();
    }
  }

  private void handleExtendEndTime() {
    if (extendEndTimeField == null || extendEndTimeField.getText().trim().isEmpty()) return;
    try {
      long minutes = Long.parseLong(extendEndTimeField.getText().trim());
      com.auction.client.ClientContext.auctionService()
          .extendEndTime(
              auctionId,
              minutes,
              response ->
                  Platform.runLater(
                      () -> {
                        if (response != null && response.isSuccess()) {
                          Alert success =
                              new Alert(
                                  Alert.AlertType.INFORMATION,
                                  "Extended end time by " + minutes + " minutes");
                          success.show();
                          extendEndTimeField.clear();
                          if (endTime != null) {
                            endTime = endTime.plusMinutes(minutes);
                            if (endTimeLabel != null)
                              endTimeLabel.setText(endTime.format(DISPLAY_FMT));
                            updateCountdown();
                          }
                        } else {
                          Alert error =
                              new Alert(
                                  Alert.AlertType.ERROR,
                                  "Failed to extend time: "
                                      + (response != null
                                          ? response.getMessage()
                                          : "Unknown error"));
                          error.show();
                        }
                      }));
    } catch (NumberFormatException e) {
      new Alert(Alert.AlertType.ERROR, "Invalid minutes value!").show();
    }
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
    this.auctionId = auctionId;
    this.currentPrice = price;
    this.startingPrice = price; // will be overridden if we later get real starting price

    if (productTitleLabel != null) productTitleLabel.setText(name);
    if (currentHighBidLabel != null) currentHighBidLabel.setText(String.format("%,.0f VND", price));
    if (totalBidsLabel != null) totalBidsLabel.setText(String.valueOf(bids));

    if (endTimeLabel != null) endTimeLabel.setText(time);
    if (startTimeLabel != null) startTimeLabel.setText(LocalDateTime.now().format(DISPLAY_FMT));

    try {
      this.endTime = LocalDateTime.parse(time, ISO_FMT);
    } catch (Exception ex) {
      try {
        this.endTime = LocalDateTime.parse(time, DISPLAY_FMT);
      } catch (Exception ignored) {
        this.endTime = null;
      }
    }
    startCountdown();

    if (priceHistoryChart != null) {
      loadChartData("month");
    }
  }

  private void loadChartData(String range) {
    if (priceHistoryChart == null) return;

    chartSeries.getData().clear();

    // Build up to MAX_CHART_POINTS data points using real price values.
    // Since the seller view has no bid history list, we create meaningful
    // reference points based on the known starting and current prices.
    java.util.List<XYChart.Data<String, Number>> points = new java.util.ArrayList<>();

    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
    LocalDateTime now = LocalDateTime.now();

    switch (range) {
      case "day" -> {
        // Simulate up to 4 reference snapshots over the past 24h
        points.add(new XYChart.Data<>(now.minusHours(20).format(fmt), startingPrice));
        points.add(new XYChart.Data<>(now.minusHours(14).format(fmt),
            startingPrice + (currentPrice - startingPrice) * 0.33));
        points.add(new XYChart.Data<>(now.minusHours(8).format(fmt),
            startingPrice + (currentPrice - startingPrice) * 0.66));
        points.add(new XYChart.Data<>(now.minusHours(2).format(fmt),
            startingPrice + (currentPrice - startingPrice) * 0.90));
      }
      case "week" -> {
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE");
        points.add(new XYChart.Data<>(now.minusDays(6).format(dayFmt), startingPrice));
        points.add(new XYChart.Data<>(now.minusDays(4).format(dayFmt),
            startingPrice + (currentPrice - startingPrice) * 0.33));
        points.add(new XYChart.Data<>(now.minusDays(2).format(dayFmt),
            startingPrice + (currentPrice - startingPrice) * 0.66));
        points.add(new XYChart.Data<>(now.minusDays(1).format(dayFmt),
            startingPrice + (currentPrice - startingPrice) * 0.90));
      }
      default -> { // month
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM");
        points.add(new XYChart.Data<>(now.minusWeeks(3).format(dateFmt), startingPrice));
        points.add(new XYChart.Data<>(now.minusWeeks(2).format(dateFmt),
            startingPrice + (currentPrice - startingPrice) * 0.33));
        points.add(new XYChart.Data<>(now.minusWeeks(1).format(dateFmt),
            startingPrice + (currentPrice - startingPrice) * 0.66));
        points.add(new XYChart.Data<>(now.minusDays(2).format(dateFmt),
            startingPrice + (currentPrice - startingPrice) * 0.90));
      }
    }

    // Always cap with the real current price as the last point
    points.add(new XYChart.Data<>("Now", currentPrice));

    int from = Math.max(0, points.size() - MAX_CHART_POINTS);
    chartSeries.getData().addAll(points.subList(from, points.size()));

    highlightActiveFilter(range);
  }

  private void highlightActiveFilter(String active) {
    String gold =
        "-fx-background-color: #FFD700; -fx-border-color: #FFD700; -fx-border-radius: 6;"
            + " -fx-background-radius: 6; -fx-text-fill: #000000; -fx-font-size: 11px;"
            + " -fx-font-weight: bold; -fx-padding: 5 14 5 14; -fx-cursor: hand;";
    String normal =
        "-fx-background-color: #222222; -fx-border-color: #3A3A3A; -fx-border-radius: 6;"
            + " -fx-background-radius: 6; -fx-text-fill: #AAAAAA; -fx-font-size: 11px; -fx-padding:"
            + " 5 14 5 14; -fx-cursor: hand;";
    if (btnDay != null) btnDay.setStyle(active.equals("day") ? gold : normal);
    if (btnWeek != null) btnWeek.setStyle(active.equals("week") ? gold : normal);
    if (btnMonth != null) btnMonth.setStyle(active.equals("month") ? gold : normal);
  }

  private void startCountdown() {
    if (countdownTimeline != null) countdownTimeline.stop();
    if (endTime == null) {
      if (endsInLabel != null) endsInLabel.setText("N/A");
      return;
    }
    countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateCountdown()));
    countdownTimeline.setCycleCount(Animation.INDEFINITE);
    countdownTimeline.play();
    updateCountdown();
  }

  private void updateCountdown() {
    if (endsInLabel == null) return;
    LocalDateTime now = LocalDateTime.now();
    if (now.isAfter(endTime)) {
      endsInLabel.setText("Ended");
      if (countdownTimeline != null) countdownTimeline.stop();
      return;
    }
    long days = ChronoUnit.DAYS.between(now, endTime);
    long hours = ChronoUnit.HOURS.between(now, endTime) % 24;
    long minutes = ChronoUnit.MINUTES.between(now, endTime) % 60;
    long seconds = ChronoUnit.SECONDS.between(now, endTime) % 60;
    endsInLabel.setText(String.format("%dd %02dh %02dm %02ds", days, hours, minutes, seconds));
  }

  public static void open(
      String icon,
      String category,
      String name,
      double price,
      int bids,
      String time,
      String status,
      String auctionId) {
    try {
      FXMLLoader loader =
          new FXMLLoader(
              SellerAuctionDetailController.class.getResource(
                  "/com/auction/client/view/SellerAuctionDetailView.fxml"));
      Parent root = loader.load();

      SellerAuctionDetailController ctrl = loader.getController();
      ctrl.setData(icon, category, name, price, bids, time, status, auctionId);

      Stage stage = new Stage();
      stage.setTitle("Seller Auction Management - " + name);
      stage.setScene(new Scene(root));
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("Error loading SellerAuctionDetailView.fxml");
    }
  }
}
