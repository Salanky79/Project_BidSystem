package com.auction.client.controller;

import com.auction.client.network.SocketClient;
import com.auction.client.service.BidService;
import com.auction.share.DTO.AuctionDetailDTO;
import com.auction.share.DTO.BidDTO;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.DTO.Response;
import com.auction.share.exceptions.ValidationException;
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
import com.auction.client.controller.components.*;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.auction.client.utils.DateTimeUtils;

import com.auction.client.service.AuctionService;

public class AuctionDetailController {

    private BidService bidService;
    private AuctionService auctionService;
    private SocketClient socketClient;

    public void setServices(BidService bidService, AuctionService auctionService, SocketClient socketClient) {
        this.bidService = bidService;
        this.auctionService = auctionService;
        this.socketClient = socketClient;
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



    // ── State ────────────────────────────────────────────────────
    private double        currentPrice;
    private double        bidStep;
    private int           totalBids;
    private String        auctionId;
    private String        icon;
    private LocalDateTime endTime;
    private AuctionCountdownTimer countdownTimer;
    // C3: Guard tránh concurrent refreshAuctionDetail() ghi đè nhau khi user bid nhanh
    private boolean isRefreshing = false;
    private BidHistoryChartManager chartManager;
    private boolean       autoBidEnabled = false;
    private List<BidDTO> bidHistory = new ArrayList<>();
    private double        startingPrice = 0;
    private String startTimeISO; // real start time from server (ISO format)
    private Consumer<Response<?>> bidPushListener;
    private int lastProcessedBidCount = -1;

    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        // Close button
        closeButton.setOnAction(e -> {
            cleanup();
            Stage stage = (Stage) closeButton.getScene().getWindow();
            openStages.remove(this.auctionId);
            stage.close();
        });

        // Place Bid button
        placeBidButton.setOnAction(e -> handlePlaceBid());
        enableAutoBidButton.setOnAction(e -> handleEnableAutoBid());
        cancelAutoBidButton.setOnAction(e -> handleCancelAutoBid());



        countdownTimer = new AuctionCountdownTimer(endsInLabel);
        countdownTimer.setOnEndedAction(() -> refreshAuctionDetail());

        chartManager = new BidHistoryChartManager(priceHistoryChart);
        if (chartManager != null) {
            chartManager.loadData(bidHistory, startTimeISO, startingPrice, currentPrice);
        }
    }

    private void cleanup() {
        if (bidPushListener != null && socketClient != null) {
            socketClient.removePushListener(bidPushListener);
            bidPushListener = null;
        }
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Public API – called by the opener (ItemCardController etc.)
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

        this.currentPrice = price;
        this.bidStep      = bidStep;
        this.totalBids    = bids;
        this.auctionId    = auctionId;
        this.icon         = icon;

        productTitleLabel.setText(name);
        currentPriceLabel.setText(String.format("%.0f VND", price));
        totalBidsLabel.setText(String.valueOf(bids));
        endTimeLabel.setText(DateTimeUtils.formatDateTimeForDisplay(time));
        startTimeLabel.setText("Loading...");
        sellerNameLabel.setText("Unknown");
        descriptionLabel.setText("Loading description...");
        if (productIconLabel != null && icon != null) {
            productIconLabel.setText(icon);
        }
        minBidLabel.setText(String.format("Minimum bid: %.0f VND", price + bidStep));
        resetAutoBidState();

        // Parse end time for countdown
        this.endTime = DateTimeUtils.parseDateTime(time);
        if (countdownTimer != null) countdownTimer.start(this.endTime);

        refreshAuctionDetail();
        registerBidPushRefresh();
    }



    // ─────────────────────────────────────────────────────────────
    // Static factory – opens a new Stage with AuctionDetailv2.fxml
    // ─────────────────────────────────────────────────────────────
    // Track các Stage đang mở theo auctionId
    private static final java.util.Map<String, Stage> openStages = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    public static void open(
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
        // Nếu đã mở rồi → focus cửa sổ cũ, không tạo mới
        Stage existingStage = openStages.get(auctionId);
        if (existingStage != null && existingStage.isShowing()) {
            existingStage.requestFocus();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    AuctionDetailController.class.getResource(
                            "/com/auction/client/view/AuctionDetailv2.fxml")
            );
            Parent root = loader.load();

            AuctionDetailController ctrl = loader.getController();
            ctrl.setServices(com.auction.client.ClientContext.bidService(), com.auction.client.ClientContext.auctionService(), com.auction.client.ClientContext.socketClient());
            ctrl.setData(icon, category, name, price, bidStep, bids, time, status, auctionId);

            Stage stage = new Stage();
            stage.setTitle("Auction – " + name);
            stage.setScene(new Scene(root));
            
            // Đăng ký vào map
            openStages.put(auctionId, stage);

            stage.setOnCloseRequest(event -> {
                ctrl.cleanup();
                openStages.remove(auctionId); // Dọn khỏi map khi đóng
            });
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Error loading AuctionDetailv2.fxml");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Handlers
    // ─────────────────────────────────────────────────────────────
    private void handlePlaceBid() {
        String input = bidInputField.getText().trim();
        if (input.isEmpty()) return;

        try {
            double amount = Double.parseDouble(input);
            bidService.placeBid(this.auctionId, amount, currentPrice, response ->
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        // A1: Server là nguồn sự thật duy nhất — không tự cập nhật giá
                        // Auto-bid có thể đã xử lý sau khi server nhận lệnh, giá thực > giá user nhập
                        bidInputField.clear();
                        refreshAuctionDetail();
                    } else {
                        showBidError(response != null ? response.getMessage() : "Lỗi kết nối máy chủ");
                    }
                })
            );
        } catch (NumberFormatException e) {
            showBidError("Giá bid không hợp lệ, vui lòng nhập số.");
        } catch (ValidationException e) {
            showBidError(e.getMessage());
        }
    }

    private void handleEnableAutoBid() {
        try {
            String maxStr = autoBidMaxInputField.getText().trim();
            String incStr = autoBidIncrementInputField.getText().trim();
            if (maxStr.isEmpty() || incStr.isEmpty()) {
                showAutoBidError("Vui lòng nhập đầy đủ giá tối đa và bước nhảy.");
                return;
            }
            double maxBid = Double.parseDouble(maxStr);
            double increment = Double.parseDouble(incStr);
            bidService.setAutoBid(
                    this.auctionId,
                    maxBid,
                    increment,
                    currentPrice,
                    response -> Platform.runLater(() -> {
                        if (response != null && response.isSuccess()) {
                            autoBidEnabled = true;
                            updateAutoBidControls();
                            autoBidStatusLabel.setStyle("-fx-text-fill: #D4AF37; -fx-font-size: 11px;");
                            autoBidStatusLabel.setText("Auto-bid is active");
                        } else {
                            showAutoBidError(response != null ? response.getMessage() : "Loi ket noi may chu");
                        }
                    })
            );
        } catch (NumberFormatException e) {
            showAutoBidError("Auto-bid không hợp lệ, vui lòng nhập số.");
        } catch (ValidationException e) {
            showAutoBidError(e.getMessage());
        }
    }

    private void handleCancelAutoBid() {
        try {
            bidService.cancelAutoBid(this.auctionId, response -> Platform.runLater(() -> {
                if (response != null && response.isSuccess()) {
                    autoBidEnabled = false;
                    updateAutoBidControls();
                    autoBidStatusLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
                    autoBidStatusLabel.setText("Auto-bid is off");
                } else {
                    showAutoBidError(response != null ? response.getMessage() : "Loi ket noi may chu");
                }
            }));
        } catch (ValidationException e) {
            showAutoBidError(e.getMessage());
        }
    }



    // ─────────────────────────────────────────────────────────────
    // Price-history chart
    // ─────────────────────────────────────────────────────────────



    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────
    private void showBidError(String message) {
        // Display inline near bid button via minBidLabel (repurposed for error)
        minBidLabel.setStyle("-fx-text-fill: #FF4444; -fx-font-size: 11px;");
        minBidLabel.setText(message);
    }

    private void showAutoBidError(String message) {
        autoBidStatusLabel.setStyle("-fx-text-fill: #FF4444; -fx-font-size: 11px;");
        autoBidStatusLabel.setText(message);
    }

    private void resetAutoBidState() {
        autoBidEnabled = false;
        updateAutoBidControls();
        autoBidStatusLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
        autoBidStatusLabel.setText("Auto-bid is off");
    }

    private void registerBidPushRefresh() {
        if (bidPushListener != null) {
            socketClient.removePushListener(bidPushListener);
        }
        bidPushListener = response -> {
            if (response != null && response.isSuccess() && response.getData() instanceof BidUpdateEvent event) {
                if (this.auctionId != null && this.auctionId.equals(event.getAuctionId())) {
                    Platform.runLater(() -> applyBidUpdate(event));
                }
            }
        };
        if (socketClient != null) {
            socketClient.addPushListener(bidPushListener);
        }
    }

    private void refreshAuctionDetail() {
        if (isRefreshing) return;
        isRefreshing = true;
        if (auctionService == null) {
            isRefreshing = false;
            return;
        }
        auctionService.getAuctionDetail(this.auctionId, response -> Platform.runLater(() -> {
            isRefreshing = false;
            if (response == null || !response.isSuccess()) return;
            if (!(response.getData() instanceof AuctionDetailDTO detail)) return;

            updatePriceInfo(detail);
            updateTimeInfo(detail);
            updateProductImage(detail);
            updateBidHistory(detail);

            if (isAuctionEnded(detail)) {
                showWinnerInfo(detail);
            }
        }));
    }

    private void updatePriceInfo(AuctionDetailDTO detail) {
        this.currentPrice = detail.getCurrentPrice();
        this.bidStep = detail.getBidStep();
        this.totalBids = detail.getBidHistory() != null ? detail.getBidHistory().size() : this.totalBids;
        this.currentPriceLabel.setText(String.format("%.0f VND", this.currentPrice));
        this.totalBidsLabel.setText(String.valueOf(this.totalBids));
        this.minBidLabel.setStyle("");
        this.minBidLabel.setText(String.format("Minimum bid: %.0f VND", this.currentPrice + this.bidStep));
        this.sellerNameLabel.setText(detail.getSellerName());
        this.descriptionLabel.setText(detail.getDescription() != null ? detail.getDescription() : "No description.");
        this.lastProcessedBidCount = detail.getBidCount();
    }

    private void updateTimeInfo(AuctionDetailDTO detail) {
        this.startTimeISO = detail.getStartTime();
        if (detail.getStartTime() != null) {
            this.startTimeLabel.setText(DateTimeUtils.formatDateTimeForDisplay(detail.getStartTime()));
        }
        if (detail.getEndTime() != null) {
            this.endTimeLabel.setText(DateTimeUtils.formatDateTimeForDisplay(detail.getEndTime()));
            this.endTime = DateTimeUtils.parseDateTime(detail.getEndTime());
        }
    }

    private void updateProductImage(AuctionDetailDTO detail) {
        if (detail.getImageUrl() != null && !detail.getImageUrl().isBlank()) {
            String currentUrl = productIconLabel.getGraphic() instanceof ImageView iv
                ? (String) iv.getUserData() : null;
            if (!detail.getImageUrl().equals(currentUrl)) {
                ImageView imageView = new ImageView();
                imageView.setFitWidth(280);
                imageView.setFitHeight(280);
                imageView.setPreserveRatio(true);
                imageView.setUserData(detail.getImageUrl());
                imageView.setImage(new javafx.scene.image.Image(detail.getImageUrl(), 280, 280, true, true, true));
                productIconLabel.setGraphic(imageView);
                productIconLabel.setText("");
            }
        } else {
            productIconLabel.setGraphic(null);
            if (icon != null) productIconLabel.setText(icon);
        }
    }

    private void updateBidHistory(AuctionDetailDTO detail) {
        this.bidHistory = detail.getBidHistory() != null
            ? detail.getBidHistory() : new ArrayList<>();
        this.startingPrice = detail.getStartingPrice();
        if (chartManager != null) {
            chartManager.loadData(bidHistory, startTimeISO, startingPrice, currentPrice);
        }
    }

    private boolean isAuctionEnded(AuctionDetailDTO detail) {
        return "FINISHED".equals(detail.getStatus())
            || (this.endTime != null && LocalDateTime.now().isAfter(this.endTime));
    }

    private void applyBidUpdate(BidUpdateEvent event) {
        if (event == null) return;
        
        if (event.getBidCount() <= lastProcessedBidCount) {
            return; // Ignore outdated bid update
        }
        lastProcessedBidCount = event.getBidCount();

        this.currentPrice = event.getCurrentHighestBid();
        this.currentPriceLabel.setText(String.format("%.0f VND", this.currentPrice));
        this.minBidLabel.setStyle(""); // clear style lỗi nếu có
        this.minBidLabel.setText(String.format("Minimum bid: %.0f VND", this.currentPrice + this.bidStep));

        if (this.bidHistory == null) {
            this.bidHistory = new java.util.ArrayList<>();
        }

        boolean exists = false;
        for (BidDTO b : this.bidHistory) {
            if (Double.compare(b.getAmount(), event.getAmount()) == 0
                    && b.getBidderName().equals(event.getBidderName())
                    && b.getTimestamp().equals(event.getBidTime())) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            this.bidHistory.add(new BidDTO(event.getBidderName(), event.getAmount(), event.getBidTime()));
            this.totalBids = this.bidHistory.size();
            this.totalBidsLabel.setText(String.valueOf(this.totalBids));
            if (chartManager != null) chartManager.appendPoint(this.currentPrice);
        }

        if (winnerNameLabel != null) {
            winnerNameLabel.setText(event.getBidderName());
        }
    }

    private void updateAutoBidControls() {
        enableAutoBidButton.setDisable(autoBidEnabled);
        cancelAutoBidButton.setDisable(!autoBidEnabled);
        autoBidMaxInputField.setDisable(autoBidEnabled);
        autoBidIncrementInputField.setDisable(autoBidEnabled);
    }

    private void showWinnerInfo(com.auction.share.DTO.AuctionDetailDTO detail) {
        if (winnerCard != null && winnerSpacer != null && winnerNameLabel != null) {
            winnerSpacer.setVisible(true);
            winnerSpacer.setManaged(true);
            winnerCard.setVisible(true);
            winnerCard.setManaged(true);
            if (detail != null && detail.getHighestBidderName() != null && !detail.getHighestBidderName().trim().isEmpty()) {
                winnerNameLabel.setText(detail.getHighestBidderName());
            } else {
                winnerNameLabel.setText("No winner identified (No bids)");
            }
        }
    }
}
