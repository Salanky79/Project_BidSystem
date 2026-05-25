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
  @FXML private LineChart<String, Number> priceHistoryChart;

  // ── STATS ───────────────────────────────────────────────────
  @FXML private Label currentHighBidLabel;
  @FXML private Label totalBidsLabel;
  @FXML private Label startTimeLabel;
  @FXML private Label endTimeLabel;
  @FXML private Label endsInLabel;

  // ── CONTROLS ────────────────────────────────────────────────
  @FXML private Button cancelAuctionButton;
  @FXML private TextField bidStepField;
  @FXML private Button saveBidStepButton;



  private String auctionId;
  private double currentPrice;
  private double startingPrice = 0;
  private LocalDateTime endTime;
  private Timeline countdownTimeline;
  private static final int MAX_CHART_POINTS = 5;
  // Persistent series – never replaced, only data points are added/removed
  private final XYChart.Series<String, Number> chartSeries = new XYChart.Series<>();
  private String startTimeISO; // real start time from server (ISO format)
  private String endTimeISO;   // real end time from server (ISO format)
  private java.util.List<com.auction.share.DTO.BidDTO> bidHistory = new java.util.ArrayList<>();

  private static final DateTimeFormatter DISPLAY_FMT =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
  private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
  private static final DateTimeFormatter ALT_DB_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @FXML
  public void initialize() {
    if (closeButton != null) {
      closeButton.setOnAction(
          e -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
          });
    }

    if (priceHistoryChart != null) {
      NumberAxis yAxis = (NumberAxis) priceHistoryChart.getYAxis();
      yAxis.setAutoRanging(true);
      yAxis.setForceZeroInRange(false);
      priceHistoryChart.getData().add(chartSeries);
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

    if (saveBidStepButton != null) {
      saveBidStepButton.setOnAction(e -> handleSetBidStep());
    }
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
    this.endTimeISO = time;

    if (productTitleLabel != null) productTitleLabel.setText(name);
    if (currentHighBidLabel != null) currentHighBidLabel.setText(String.format("%,.0f VND", price));
    if (totalBidsLabel != null) totalBidsLabel.setText(String.valueOf(bids));

    if (endTimeLabel != null) endTimeLabel.setText(time);
    if (startTimeLabel != null) startTimeLabel.setText("Loading...");

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

    // Fetch real detail from server to get startTime, bidHistory, etc.
    com.auction.client.ClientContext.auctionService().getAuctionDetail(this.auctionId, response -> Platform.runLater(() -> {
      if (response != null && response.isSuccess() && response.getData() instanceof com.auction.share.DTO.AuctionDetailDTO detail) {
        this.currentPrice = detail.getCurrentPrice();
        this.startingPrice = detail.getStartingPrice();
        this.bidHistory = detail.getBidHistory() != null ? detail.getBidHistory() : new java.util.ArrayList<>();

        if (currentHighBidLabel != null) currentHighBidLabel.setText(String.format("%,.0f VND", this.currentPrice));
        if (totalBidsLabel != null) totalBidsLabel.setText(String.valueOf(this.bidHistory.size()));

        // Update start/end time labels with real data from server (ISO format)
        this.startTimeISO = detail.getStartTime();
        if (detail.getStartTime() != null && startTimeLabel != null) {
          startTimeLabel.setText(formatDateTimeForDisplay(detail.getStartTime()));
        }
        if (detail.getEndTime() != null) {
          this.endTimeISO = detail.getEndTime();
          if (endTimeLabel != null) endTimeLabel.setText(formatDateTimeForDisplay(detail.getEndTime()));
          LocalDateTime parsedEnd = parseDateTime(detail.getEndTime());
          if (parsedEnd != null) {
            this.endTime = parsedEnd;
            startCountdown();
          }
        }

        if (priceHistoryChart != null) {
          loadChartData();
        }
      } else {
        // Fallback: load chart with what we have
        if (priceHistoryChart != null) {
          loadChartData();
        }
      }
    }));
  }

  private void loadChartData() {
    if (priceHistoryChart == null) return;

    chartSeries.getData().clear();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    // Collect qualifying bids sorted oldest-first
    java.util.List<XYChart.Data<String, Number>> points = new java.util.ArrayList<>();

    if (bidHistory != null && !bidHistory.isEmpty()) {
      java.util.List<com.auction.share.DTO.BidDTO> sortedBids = new java.util.ArrayList<>(bidHistory);
      sortedBids.sort((b1, b2) -> {
        try {
          return LocalDateTime.parse(b1.getTimestamp(), ISO_FMT)
                             .compareTo(LocalDateTime.parse(b2.getTimestamp(), ISO_FMT));
        } catch (Exception e) { return 0; }
      });

      for (com.auction.share.DTO.BidDTO bid : sortedBids) {
        try {
          LocalDateTime bidTime = LocalDateTime.parse(bid.getTimestamp(), ISO_FMT);
          points.add(new XYChart.Data<>(bidTime.format(formatter), bid.getAmount()));
        } catch (Exception ignored) {}
      }
    }

    // Always ensure at least a starting anchor
    if (points.isEmpty()) {
      String startLabel = "Start";
      if (startTimeISO != null) {
        try {
          startLabel = LocalDateTime.parse(startTimeISO, ISO_FMT).format(formatter);
        } catch (Exception ignored) {}
      }
      points.add(new XYChart.Data<>(startLabel, startingPrice > 0 ? startingPrice : currentPrice));
    }

    // Keep only the last MAX_CHART_POINTS entries
    int from = Math.max(0, points.size() - MAX_CHART_POINTS);
    chartSeries.getData().addAll(points.subList(from, points.size()));
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
