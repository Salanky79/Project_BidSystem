package com.auction.client.controller;


import com.auction.client.service.BidService;
import com.auction.client.service.AuctionService;
import com.auction.client.utils.AuctionCountdownTimer;
import com.auction.client.utils.BidHistoryChartManager;
import com.auction.client.service.AuctionPushRegistry;
import com.auction.share.DTO.AuctionDetailDTO;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.exceptions.ValidationException;
import com.auction.client.utils.DateTimeUtils;
import com.auction.client.utils.FormatUtils;
import com.auction.client.utils.NotificationManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class AuctionDetailController {

    private BidService bidService;
    private AuctionService auctionService;

    public void setServices(BidService bidService, AuctionService auctionService) {
        this.bidService = bidService;
        this.auctionService = auctionService;
    }

    // ── HEADER ──────────────────────────────────────────────────
    @FXML private Label  productTitleLabel;
    @FXML private Button closeButton;

    // ── LEFT COLUMN ─────────────────────────────────────────────
    @FXML private LineChart<String, Number> priceHistoryChart;

    @FXML private Label sellerNameLabel;
    @FXML private Label ratingLabel;
    @FXML private Label descriptionLabel;
    @FXML private VBox winnerCard;
    @FXML private Region winnerSpacer;
    @FXML private Label winnerNameLabel;

    // ── RIGHT COLUMN ─────────────────────────────────────────────
    @FXML private Label     currentPriceLabel;
    @FXML private Label     totalBidsLabel;
    @FXML private Label     startTimeLabel;
    @FXML private Label     endTimeLabel;
    @FXML private Label     endsInLabel;
    @FXML private Label     productIconLabel;
    @FXML private TextField bidInputField;
    @FXML private Button    placeBidButton;
    @FXML private Label     minBidLabel;
    @FXML private TextField autoBidMaxInputField;
    @FXML private TextField autoBidIncrementInputField;
    @FXML private Button    enableAutoBidButton;
    @FXML private Button    cancelAutoBidButton;
    @FXML private Label     autoBidStatusLabel;

    // ── Visual/Countdown / Chart state ────────────────────────────
    private AuctionCountdownTimer countdownTimer;
    private BidHistoryChartManager chartManager;


    // ── ViewModel & PushHandler ──────────────────────────────────
    private final AuctionDetailViewModel viewModel = new AuctionDetailViewModel();
    private AuctionPushRegistry pushHandler;

    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        // Close button
        closeButton.setOnAction(e -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.fireEvent(new javafx.stage.WindowEvent(stage, javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        // Event handlers
        placeBidButton.setOnAction(e -> handlePlaceBid());
        enableAutoBidButton.setOnAction(e -> handleEnableAutoBid());
        cancelAutoBidButton.setOnAction(e -> handleCancelAutoBid());
        
        FormatUtils.setupNumberField(bidInputField);
        FormatUtils.setupNumberField(autoBidMaxInputField);
        FormatUtils.setupNumberField(autoBidIncrementInputField);

        countdownTimer = new AuctionCountdownTimer(endsInLabel);
        countdownTimer.setOnEndedAction(() -> {
            Platform.runLater(() -> {
                placeBidButton.setDisable(true);
                enableAutoBidButton.setDisable(true);
            });
            refreshAuctionDetail();
        });

        chartManager = new BidHistoryChartManager(priceHistoryChart);
    }

    public void cleanup() {
        if (pushHandler != null) {
            pushHandler.unregister();
        }
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Public API – called by the opener (AppNavigator)
    // ─────────────────────────────────────────────────────────────
    public void setData(
            String icon,
            String category,
            String name,
            double price,
            double bidStep,
            int    bids,
            String time,
            String status,
            String auctionId
    ) {
        if (auctionId == null || auctionId.isBlank()) {
            System.err.println("AuctionDetailController: auctionId is null or blank!");
            return;
        }

        viewModel.initData(icon, category, name, price, bidStep, bids, time, status, auctionId);

        productTitleLabel.setText(viewModel.getName());
        currentPriceLabel.setText(String.format("%,.0f VND", viewModel.getCurrentPrice()));
        totalBidsLabel.setText(String.valueOf(viewModel.getTotalBids()));
        endTimeLabel.setText(DateTimeUtils.formatDateTimeForDisplay(time));
        startTimeLabel.setText("Loading...");
        sellerNameLabel.setText("Unknown");
        descriptionLabel.setText("Loading description...");
        if (productIconLabel != null && viewModel.getIcon() != null) {
            productIconLabel.setText(viewModel.getIcon());
        }
        minBidLabel.setText(String.format("Minimum bid: %,.0f VND", viewModel.getMinimumBid()));
        resetAutoBidState();

        if (countdownTimer != null) {
            countdownTimer.start(viewModel.getEndTime());
        }

        refreshAuctionDetail();
        registerBidPushRefresh();
    }

    // ─────────────────────────────────────────────────────────────
    // Handlers
    // ─────────────────────────────────────────────────────────────
    private void handlePlaceBid() {
        String input = bidInputField.getText().trim();
        if (input.isEmpty()) return;

        try {
            double amount = FormatUtils.parseFormattedNumber(input);
            bidService.placeBid(viewModel.getAuctionId(), amount, viewModel.getMinimumBid(), response ->
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        bidInputField.clear();
                        NotificationManager.showSuccess("Bid placed successfully!");
                        refreshAuctionDetail();
                    } else {
                        showBidError(response != null ? response.getMessage() : "Server connection error");
                    }
                })
            );
        } catch (NumberFormatException e) {
            showBidError("Invalid bid amount, please enter a number.");
        } catch (ValidationException e) {
            showBidError(e.getMessage());
        }
    }

    private void handleEnableAutoBid() {
        try {
            String maxStr = autoBidMaxInputField.getText().trim();
            String incStr = autoBidIncrementInputField.getText().trim();
            if (maxStr.isEmpty() || incStr.isEmpty()) {
                showAutoBidError("Please enter both max bid and increment.");
                return;
            }
            double maxBid = FormatUtils.parseFormattedNumber(maxStr);
            double increment = FormatUtils.parseFormattedNumber(incStr);
            bidService.setAutoBid(
                    viewModel.getAuctionId(),
                    maxBid,
                    increment,
                    viewModel.getCurrentPrice(),
                    response -> Platform.runLater(() -> {
                        if (response != null && response.isSuccess()) {
                            viewModel.setAutoBidEnabled(true);
                            updateAutoBidControls();
                            autoBidStatusLabel.setStyle("-fx-text-fill: #D4AF37; -fx-font-size: 11px;");
                            autoBidStatusLabel.setText("Auto-bid is active");
                        } else {
                            showAutoBidError(response != null ? response.getMessage() : "Server connection error");
                        }
                    })
            );
        } catch (NumberFormatException e) {
            showAutoBidError("Invalid auto-bid amount, please enter a number.");
        } catch (ValidationException e) {
            showAutoBidError(e.getMessage());
        }
    }

    private void handleCancelAutoBid() {
        try {
            bidService.cancelAutoBid(viewModel.getAuctionId(), response -> Platform.runLater(() -> {
                if (response != null && response.isSuccess()) {
                    viewModel.setAutoBidEnabled(false);
                    updateAutoBidControls();
                    autoBidStatusLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
                    autoBidStatusLabel.setText("Auto-bid is off");
                } else {
                    showAutoBidError(response != null ? response.getMessage() : "Server connection error");
                }
            }));
        } catch (ValidationException e) {
            showAutoBidError(e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────
    private void showBidError(String message) {
        minBidLabel.setStyle("-fx-text-fill: #FF4444; -fx-font-size: 11px;");
        minBidLabel.setText(message);
    }

    private void showAutoBidError(String message) {
        autoBidStatusLabel.setStyle("-fx-text-fill: #FF4444; -fx-font-size: 11px;");
        autoBidStatusLabel.setText(message);
    }

    private void resetAutoBidState() {
        viewModel.setAutoBidEnabled(false);
        updateAutoBidControls();
        autoBidStatusLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
        autoBidStatusLabel.setText("Auto-bid is off");
    }

    private void registerBidPushRefresh() {
        if (pushHandler != null) {
            pushHandler.unregister();
        }
        pushHandler = auctionService.createPushRegistry(viewModel.getAuctionId());
        pushHandler.setOnBidUpdate(event -> {
            if (viewModel.applyBidUpdate(event)) {
                applyBidUpdateUI(event);
            }
        });
        pushHandler.setOnAuctionCancelled(this::refreshAuctionDetail);
        pushHandler.setOnAuctionFinished(this::refreshAuctionDetail);
        pushHandler.setOnBidStepUpdate(newBidStep -> {
            viewModel.setBidStep(newBidStep);
            minBidLabel.setStyle("");
            minBidLabel.setText(String.format("Minimum bid: %,.0f VND", viewModel.getMinimumBid()));
        });
        pushHandler.register();
    }

    private void applyBidUpdateUI(BidUpdateEvent event) {
        currentPriceLabel.setText(String.format("%,.0f VND", viewModel.getCurrentPrice()));
        minBidLabel.setStyle(""); // clear style
        minBidLabel.setText(String.format("Minimum bid: %,.0f VND", viewModel.getMinimumBid()));
        totalBidsLabel.setText(String.valueOf(viewModel.getTotalBids()));
        if (chartManager != null) {
            chartManager.appendPoint(viewModel.getCurrentPrice(), event.getBidTime());
        }
        if (winnerNameLabel != null) {
            winnerNameLabel.setText(event.getBidderName());
        }
        if (event.getNewEndTime() != null) {
            if (countdownTimer != null) {
                countdownTimer.start(viewModel.getEndTime());
            }
            endTimeLabel.setText(DateTimeUtils.formatDateTimeForDisplay(viewModel.getEndTime().toString()));
        }
    }

    private void refreshAuctionDetail() {
        if (auctionService == null) {
            return;
        }
        auctionService.getAuctionDetail(viewModel.getAuctionId(), response -> Platform.runLater(() -> {
            if (response == null || !response.isSuccess()) return;
            if (!(response.getData() instanceof AuctionDetailDTO detail)) return;

            viewModel.updateFrom(detail);
            updateUIFromViewModel();
        }));
    }

    private void updateUIFromViewModel() {
        currentPriceLabel.setText(String.format("%,.0f VND", viewModel.getCurrentPrice()));
        totalBidsLabel.setText(String.valueOf(viewModel.getTotalBids()));
        minBidLabel.setStyle("");
        minBidLabel.setText(String.format("Minimum bid: %,.0f VND", viewModel.getMinimumBid()));
        sellerNameLabel.setText(viewModel.getSellerName());
        descriptionLabel.setText(viewModel.getDescription());

        if (viewModel.getStartTimeISO() != null) {
            startTimeLabel.setText(DateTimeUtils.formatDateTimeForDisplay(viewModel.getStartTimeISO()));
        }
        if (viewModel.getEndTime() != null) {
            endTimeLabel.setText(DateTimeUtils.formatDateTimeForDisplay(viewModel.getEndTime().toString()));
        }



        if (chartManager != null) {
            chartManager.loadData(viewModel.getBidHistory(), viewModel.getStartTimeISO(), viewModel.getStartingPrice(), viewModel.getCurrentPrice());
        }

        if (viewModel.isEnded()) {
            if (countdownTimer != null) {
                countdownTimer.stop();
            }
            placeBidButton.setDisable(true);
            enableAutoBidButton.setDisable(true);
            if ("CANCELED".equals(viewModel.getStatus())) {
                endsInLabel.setText("Cancelled");
            }
            showWinnerInfoUI();
        } else {
            if (countdownTimer != null && viewModel.getEndTime() != null) {
                countdownTimer.start(viewModel.getEndTime());
            }
            placeBidButton.setDisable(false);
            enableAutoBidButton.setDisable(false);
        }
    }


    private void showWinnerInfoUI() {
        if (winnerCard != null && winnerSpacer != null && winnerNameLabel != null) {
            winnerSpacer.setVisible(true);
            winnerSpacer.setManaged(true);
            winnerCard.setVisible(true);
            winnerCard.setManaged(true);
            if ("CANCELED".equals(viewModel.getStatus())) {
                winnerNameLabel.setText("Auction Cancelled");
            } else if (viewModel.getHighestBidderName() != null && !viewModel.getHighestBidderName().trim().isEmpty()) {
                winnerNameLabel.setText(viewModel.getHighestBidderName());
            } else {
                winnerNameLabel.setText("No winner identified (No bids)");
            }
        }
    }

    private void updateAutoBidControls() {
        boolean autoBidEnabled = viewModel.isAutoBidEnabled();
        enableAutoBidButton.setDisable(autoBidEnabled);
        cancelAutoBidButton.setDisable(!autoBidEnabled);
        autoBidMaxInputField.setDisable(autoBidEnabled);
        autoBidIncrementInputField.setDisable(autoBidEnabled);
    }
}
