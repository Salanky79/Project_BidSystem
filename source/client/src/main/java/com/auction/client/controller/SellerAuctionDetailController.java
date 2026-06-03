package com.auction.client.controller;

import com.auction.client.service.AuctionService;

import com.auction.client.utils.AuctionCountdownTimer;
import com.auction.client.utils.BidHistoryChartManager;
import com.auction.client.service.AuctionPushRegistry;
import com.auction.client.utils.NotificationManager;
import com.auction.client.utils.DateTimeUtils;
import com.auction.share.DTO.AuctionDetailDTO;
import com.auction.share.DTO.BidDTO;
import com.auction.client.utils.FormatUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SellerAuctionDetailController {

  private AuctionService auctionService;

  public void setServices(AuctionService auctionService) {
    this.auctionService = auctionService;
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
  @FXML private VBox controlPanelCard;
  @FXML private VBox bidStepContainer;
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
  private List<BidDTO> bidHistory = new ArrayList<>();
  
  /** Callback được gọi sau khi cancel auction thành công — để Dashboard invalidate cache */
  private Runnable dashboardInvalidator;
  private AuctionPushRegistry pushHandler;

  public void setDashboardInvalidator(Runnable dashboardInvalidator) {
    this.dashboardInvalidator = dashboardInvalidator;
  }

  private void registerBidPushRefresh() {
    if (pushHandler != null) {
      pushHandler.unregister();
    }
    pushHandler = auctionService.createPushRegistry(this.auctionId);
    pushHandler.setOnBidUpdate(event -> refreshData());
    pushHandler.setOnAuctionCancelled(this::refreshData);
    pushHandler.register();
  }

  private void refreshData() {
    if (auctionService == null) return;
    auctionService.getAuctionDetail(this.auctionId, response -> Platform.runLater(() -> {
      if (response != null && response.isSuccess() && response.getData() instanceof AuctionDetailDTO detail) {
        this.currentPrice = detail.getCurrentPrice();
        if (currentHighBidLabel != null) {
          currentHighBidLabel.setText(String.format("%,.0f VND", this.currentPrice));
        }
        if (totalBidsLabel != null) {
          int totalBids = detail.getBidHistory() != null ? detail.getBidHistory().size() : 0;
          totalBidsLabel.setText(String.valueOf(totalBids));
        }
        if (chartManager != null && detail.getBidHistory() != null) {
          chartManager.loadData(detail.getBidHistory(), startTimeISO, startingPrice, currentPrice);
        }
        updateVisibilityBasedOnStatus(detail.getStatus());
      }
    }));
  }

  @FXML
  public void initialize() {
    if (closeButton != null) {
      closeButton.setOnAction(
          e -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.fireEvent(new javafx.stage.WindowEvent(stage, javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST));
          });
    }

    countdownTimer = new AuctionCountdownTimer(endsInLabel);
    chartManager = new BidHistoryChartManager(priceHistoryChart);

    if (cancelAuctionButton != null) {
      cancelAuctionButton.setOnAction(
          e -> {
            NotificationManager.showWarning("Cancelling auction...");
            if (auctionService == null) return;
            auctionService.cancelAuction(
                auctionId,
                response -> Platform.runLater(() -> {
                  if (response != null && response.isSuccess()) {
                    cancelAuctionButton.setText("Cancelled");
                    cancelAuctionButton.setDisable(true);
                    if (endsInLabel != null) {
                      endsInLabel.setText("Cancelled");
                    }
                    if (countdownTimer != null) {
                      countdownTimer.stop();
                    }
                    if (dashboardInvalidator != null) {
                      dashboardInvalidator.run();
                    }
                    NotificationManager.showSuccess("Auction cancelled successfully!");
                  } else {
                    NotificationManager.showError(
                        "Cannot cancel: "
                            + (response != null ? response.getMessage() : "Unknown error"));
                  }
                }));
          });
    }

    if (saveBidStepButton != null) {
      saveBidStepButton.setOnAction(e -> handleSetBidStep());
    }
    if (bidStepField != null) {
      FormatUtils.setupNumberField(bidStepField);
    }
  }

  public void cleanup() {
    if (countdownTimer != null) {
      countdownTimer.stop();
    }
    if (pushHandler != null) {
      pushHandler.unregister();
    }
  }

  private void handleSetBidStep() {
    if (bidStepField == null || bidStepField.getText().trim().isEmpty()) return;
    try {
      double step = FormatUtils.parseFormattedNumber(bidStepField.getText().trim());
      if (auctionService == null) return;
      auctionService.setBidStep(
          auctionId,
          step,
          response -> Platform.runLater(() -> {
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
    this.startingPrice = price;

    if (productTitleLabel != null) productTitleLabel.setText(name);
    if (currentHighBidLabel != null) currentHighBidLabel.setText(String.format("%,.0f VND", price));
    if (totalBidsLabel != null) totalBidsLabel.setText(String.valueOf(bids));

    if (endTimeLabel != null) endTimeLabel.setText(time);
    if (startTimeLabel != null) startTimeLabel.setText("Loading...");

    this.endTime = DateTimeUtils.parseDateTime(time);
    boolean isCancelled = "CANCELED".equalsIgnoreCase(status);
    if (countdownTimer != null) {
      if (!isCancelled) {
        countdownTimer.start(this.endTime);
      } else {
        countdownTimer.stop();
        if (endsInLabel != null) {
          endsInLabel.setText("Cancelled");
        }
      }
    }

    // Initial visibility check from passed status
    updateVisibilityBasedOnStatus(status);

    if (auctionService != null) {
      auctionService.getAuctionDetail(this.auctionId, response -> Platform.runLater(() -> {
        if (response != null && response.isSuccess() && response.getData() instanceof AuctionDetailDTO detail) {
          this.currentPrice = detail.getCurrentPrice();
          this.startingPrice = detail.getStartingPrice();
          this.bidHistory = detail.getBidHistory() != null ? detail.getBidHistory() : new ArrayList<>();

          if (currentHighBidLabel != null) {
            currentHighBidLabel.setText(String.format("%,.0f VND", this.currentPrice));
          }
          if (totalBidsLabel != null) {
            totalBidsLabel.setText(String.valueOf(this.bidHistory.size()));
          }

          this.startTimeISO = detail.getStartTime();
          if (detail.getStartTime() != null && startTimeLabel != null) {
            startTimeLabel.setText(DateTimeUtils.formatDateTimeForDisplay(detail.getStartTime()));
          }
          if (detail.getEndTime() != null) {
            if (endTimeLabel != null) {
              endTimeLabel.setText(DateTimeUtils.formatDateTimeForDisplay(detail.getEndTime()));
            }
            LocalDateTime parsedEnd = DateTimeUtils.parseDateTime(detail.getEndTime());
            if (parsedEnd != null) {
              this.endTime = parsedEnd;
              boolean detailCancelled = "CANCELED".equalsIgnoreCase(detail.getStatus());
              if (countdownTimer != null) {
                if (!detailCancelled) {
                  countdownTimer.start(this.endTime);
                } else {
                  countdownTimer.stop();
                  if (endsInLabel != null) {
                    endsInLabel.setText("Cancelled");
                  }
                }
              }
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
          
          // Recheck visibility from detailed status loaded from server
          updateVisibilityBasedOnStatus(detail.getStatus());
        } else {
          if (chartManager != null) {
            chartManager.loadData(bidHistory, startTimeISO, startingPrice, currentPrice);
          }
        }
      }));
    }
  }

  private void updateVisibilityBasedOnStatus(String status) {
    boolean isCancelled = "CANCELED".equalsIgnoreCase(status);
    if (cancelAuctionButton != null) {
      cancelAuctionButton.setVisible(!isCancelled);
      cancelAuctionButton.setManaged(!isCancelled);
    }
    if (bidStepContainer != null) {
      bidStepContainer.setVisible(!isCancelled);
      bidStepContainer.setManaged(!isCancelled);
    }
    if (controlPanelCard != null) {
      controlPanelCard.setVisible(!isCancelled);
      controlPanelCard.setManaged(!isCancelled);
    }
    if (isCancelled) {
      if (countdownTimer != null) {
        countdownTimer.stop();
      }
      if (endsInLabel != null) {
        endsInLabel.setText("Cancelled");
      }
    }
  }
}
