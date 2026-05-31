package com.auction.client.controller;

import com.auction.client.service.AuctionService;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.auction.client.utils.NotificationManager;

import com.auction.client.utils.DateTimeUtils;
import com.auction.client.controller.components.AuctionCountdownTimer;
import com.auction.client.controller.components.BidHistoryChartManager;

public class SellerAuctionDetailController {

  private AuctionService auctionService;
  private com.auction.client.network.SocketClient socketClient;

  public void setServices(AuctionService auctionService, com.auction.client.network.SocketClient socketClient) {
    this.auctionService = auctionService;
    this.socketClient = socketClient;
  }

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
  @FXML private Label winnerIdLabel;

  // ── CONTROLS ────────────────────────────────────────────────
  @FXML private Button cancelAuctionButton;
  @FXML private TextField bidStepField;
  @FXML private Button saveBidStepButton;



  private String auctionId;
  private double currentPrice;
  private double startingPrice = 0;
  private LocalDateTime endTime;
  private AuctionCountdownTimer countdownTimer;
  private BidHistoryChartManager chartManager;
  private String startTimeISO; // real start time from server (ISO format)
  private java.util.List<com.auction.share.DTO.BidDTO> bidHistory = new java.util.ArrayList<>();
  /** E2: Callback được gọi sau khi cancel auction thành công — để Dashboard invalidate cache */
  private Runnable dashboardInvalidator;
  private java.util.function.Consumer<com.auction.share.DTO.Response<?>> bidPushListener;

  private void registerBidPushRefresh() {
      if (bidPushListener != null && socketClient != null) {
          socketClient.removePushListener(bidPushListener);
      }
      bidPushListener = response -> {
          if (response != null && response.isSuccess() && response.getData() instanceof com.auction.share.DTO.BidUpdateEvent event) {
              if (this.auctionId != null && this.auctionId.equals(event.getAuctionId())) {
                  Platform.runLater(this::refreshData);
              }
          }
      };
      if (socketClient != null) {
          socketClient.addPushListener(bidPushListener);
      }
  }

  private void refreshData() {
      if (auctionService == null) return;
      auctionService.getAuctionDetail(this.auctionId, response -> Platform.runLater(() -> {
          if (response != null && response.isSuccess() && response.getData() instanceof com.auction.share.DTO.AuctionDetailDTO detail) {
              this.currentPrice = detail.getCurrentPrice();
              if (currentHighBidLabel != null) {
                  currentHighBidLabel.setText(String.format("%.0f VND", this.currentPrice));
              }
              if (totalBidsLabel != null) {
                  int totalBids = detail.getBidHistory() != null ? detail.getBidHistory().size() : 0;
                  totalBidsLabel.setText(String.valueOf(totalBids));
              }
              if (chartManager != null && detail.getBidHistory() != null) {
                  chartManager.loadData(detail.getBidHistory(), startTimeISO, startingPrice, currentPrice);
              }
          }
      }));
  }

  @FXML
  public void initialize() {
    if (closeButton != null) {
      closeButton.setOnAction(
          e -> {
            cleanup();
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
          });
    }

    countdownTimer = new AuctionCountdownTimer(endsInLabel);
    chartManager = new BidHistoryChartManager(priceHistoryChart);

    if (cancelAuctionButton != null) {
      cancelAuctionButton.setOnAction(
          e -> {
            // G2: Dùng NotificationManager thay Alert.CONFIRMATION
            NotificationManager.showWarning("Đang hủy phiên đấu giá...");
            if (auctionService == null) return;
            auctionService
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
                                if (countdownTimer != null) countdownTimer.stop();
                                // E2: Notify Dashboard to refresh
                                if (dashboardInvalidator != null) dashboardInvalidator.run();
                                NotificationManager.showSuccess("Đã hủy phiên đấu giá thành công!");
                              } else {
                                NotificationManager.showError(
                                    "Không thể hủy: "
                                        + (response != null
                                            ? response.getMessage()
                                            : "Unknown error"));
                              }
                            }));
          });
    }

    if (saveBidStepButton != null) {
      saveBidStepButton.setOnAction(e -> handleSetBidStep());
    }
  }

  private void cleanup() {
    if (countdownTimer != null) {
      countdownTimer.stop();
    }
    if (bidPushListener != null && socketClient != null) {
        socketClient.removePushListener(bidPushListener);
        bidPushListener = null;
    }
  }

  private void handleSetBidStep() {
    if (bidStepField == null || bidStepField.getText().trim().isEmpty()) return;
    try {
      double step = Double.parseDouble(bidStepField.getText().trim());
      if (auctionService == null) return;
      auctionService
          .setBidStep(
              auctionId,
              step,
              response ->
                  Platform.runLater(
                      () -> {
                        if (response != null && response.isSuccess()) {
                          NotificationManager.showSuccess("Bid step updated to " + step);
                          bidStepField.clear();
                        } else {
                          NotificationManager.showError("Failed to update: " + (response != null ? response.getMessage() : "Unknown error"));
                        }
                      }));
    } catch (NumberFormatException e) {
      NotificationManager.showError("Invalid bid step value!");
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
    registerBidPushRefresh();
    this.currentPrice = price;
    this.startingPrice = price; // will be overridden if we later get real starting price

    if (productTitleLabel != null) productTitleLabel.setText(name);
    if (currentHighBidLabel != null) currentHighBidLabel.setText(String.format("%,.0f VND", price));
    if (totalBidsLabel != null) totalBidsLabel.setText(String.valueOf(bids));

    if (endTimeLabel != null) endTimeLabel.setText(time);
    if (startTimeLabel != null) startTimeLabel.setText("Loading...");

    this.endTime = DateTimeUtils.parseDateTime(time);
    if (countdownTimer != null) countdownTimer.start(this.endTime);

    // Fetch real detail from server to get startTime, bidHistory, etc.
    if (auctionService != null) {
      auctionService.getAuctionDetail(this.auctionId, response -> Platform.runLater(() -> {
        if (response != null && response.isSuccess() && response.getData() instanceof com.auction.share.DTO.AuctionDetailDTO detail) {
        this.currentPrice = detail.getCurrentPrice();
        this.startingPrice = detail.getStartingPrice();
        this.bidHistory = detail.getBidHistory() != null ? detail.getBidHistory() : new java.util.ArrayList<>();

        if (currentHighBidLabel != null) currentHighBidLabel.setText(String.format("%,.0f VND", this.currentPrice));
        if (totalBidsLabel != null) totalBidsLabel.setText(String.valueOf(this.bidHistory.size()));

        // Update start/end time labels with real data from server (ISO format)
        this.startTimeISO = detail.getStartTime();
        if (detail.getStartTime() != null && startTimeLabel != null) {
          startTimeLabel.setText(DateTimeUtils.formatDateTimeForDisplay(detail.getStartTime()));
        }
        if (detail.getEndTime() != null) {
          if (endTimeLabel != null) endTimeLabel.setText(DateTimeUtils.formatDateTimeForDisplay(detail.getEndTime()));
          LocalDateTime parsedEnd = DateTimeUtils.parseDateTime(detail.getEndTime());
          if (parsedEnd != null) {
            this.endTime = parsedEnd;
            if (countdownTimer != null) countdownTimer.start(this.endTime);
          }
        }

        if (winnerIdLabel != null) {
          if (detail.getHighestBidderName() != null && !detail.getHighestBidderName().isEmpty()) {
            String winnerText = detail.getHighestBidderName();
            if (detail.getHighestBidderUsername() != null && !detail.getHighestBidderUsername().isEmpty()) {
              winnerText += " (" + detail.getHighestBidderUsername() + ")";
            }
            winnerIdLabel.setText(winnerText);
          } else {
            winnerIdLabel.setText("No winner identified (No bids)");
          }
        }

        if (chartManager != null) {
          chartManager.loadData(bidHistory, startTimeISO, startingPrice, currentPrice);
        }
      } else {
        // Fallback: load chart with what we have
        if (chartManager != null) {
          chartManager.loadData(bidHistory, startTimeISO, startingPrice, currentPrice);
        }
      }
    }));
    }
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
      ctrl.setServices(
          com.auction.client.ClientContext.auctionService(),
          com.auction.client.ClientContext.socketClient()
      );
      ctrl.setData(icon, category, name, price, bids, time, status, auctionId);

      Stage stage = new Stage();
      stage.setTitle("Seller Auction Management - " + name);
      stage.setScene(new Scene(root));
      stage.setOnCloseRequest(e -> ctrl.cleanup());
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("Error loading SellerAuctionDetailView.fxml");
    }
  }
}
