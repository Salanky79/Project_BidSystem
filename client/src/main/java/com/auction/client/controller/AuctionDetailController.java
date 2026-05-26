package com.auction.client.controller;

import com.auction.client.ClientContext;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.auction.client.ClientContext.socketClient;

public class AuctionDetailController {

    private final BidService bidService = ClientContext.bidService();

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
    private LocalDateTime endTime;
    private Timeline      countdownTimeline;
    private boolean       autoBidEnabled = false;
    private List<BidDTO> bidHistory = new ArrayList<>();
    private double        startingPrice = 0;
    private static final int  MAX_CHART_POINTS = 5;
    private static final DateTimeFormatter CHART_FMT =
            DateTimeFormatter.ofPattern("dd/MM HH:mm");
    // Persistent series – never replaced, only data points are added/removed
    private final XYChart.Series<String, Number> chartSeries = new XYChart.Series<>();
    private String startTimeISO; // real start time from server (ISO format)
    private Consumer<Response<?>> bidPushListener;

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter ISO_FMT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        // Close button
        closeButton.setOnAction(e -> {
            if (bidPushListener != null) {
                socketClient().removePushListener(bidPushListener);
            }
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });

        // Place Bid button
        placeBidButton.setOnAction(e -> handlePlaceBid());
        enableAutoBidButton.setOnAction(e -> handleEnableAutoBid());
        cancelAutoBidButton.setOnAction(e -> handleCancelAutoBid());



        // Configure Y-axis: auto-range without forcing zero so the chart
        // zooms in near the actual bid prices instead of compressing them.
        if (priceHistoryChart != null) {
            NumberAxis yAxis = (NumberAxis) priceHistoryChart.getYAxis();
            yAxis.setAutoRanging(true);
            yAxis.setForceZeroInRange(false);
            // Attach the persistent series once
            priceHistoryChart.getData().add(chartSeries);
        }

        // Default chart data – populated after setData() provides bidHistory
        loadChartData();


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
        this.currentPrice = price;
        this.bidStep      = bidStep;
        this.totalBids    = bids;
        this.auctionId    = auctionId;

        productTitleLabel.setText(name);
        currentPriceLabel.setText(String.format("%.0f VND", price));
        totalBidsLabel.setText(String.valueOf(bids));
        endTimeLabel.setText(time);
        startTimeLabel.setText("Loading...");
        sellerNameLabel.setText("Unknown");
        descriptionLabel.setText("Loading description...");
        if (productIconLabel != null && icon != null) {
            productIconLabel.setText(icon);
        }
        minBidLabel.setText(String.format("Minimum bid: %.0f VND", price + bidStep));
        resetAutoBidState();

        // Parse end time for countdown
        try {
            this.endTime = LocalDateTime.parse(time, DISPLAY_FMT);
        } catch (Exception ex) {
            try {
                this.endTime = LocalDateTime.parse(time, DISPLAY_FMT);
            } catch (Exception ignored) {
                this.endTime = null;
            }
        }
        startCountdown();

        refreshAuctionDetail();
        registerBidPushRefresh();
    }



    // ─────────────────────────────────────────────────────────────
    // Static factory – opens a new Stage with AuctionDetailv2.fxml
    // ─────────────────────────────────────────────────────────────
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
        try {
            FXMLLoader loader = new FXMLLoader(
                    AuctionDetailController.class.getResource(
                            "/com/auction/client/view/AuctionDetailv2.fxml")
            );
            Parent root = loader.load();

            AuctionDetailController ctrl = loader.getController();
            ctrl.setData(icon, category, name, price, bidStep, bids, time, status, auctionId);

            Stage stage = new Stage();
            stage.setTitle("Auction – " + name);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
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
            bidService.placeBid(this.auctionId, input, currentPrice, response ->
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        currentPrice = Double.parseDouble(input);
                        totalBids++;
                        currentPriceLabel.setText(String.format("%.0f VND", currentPrice));
                        totalBidsLabel.setText(String.valueOf(totalBids));
                        minBidLabel.setText(String.format("Minimum bid: %.0f VND", currentPrice + bidStep));
                        bidInputField.clear();

                        String username = ClientContext.userService().getSessionManager().getCurrentUserId();
                        bidHistory.add(new com.auction.share.DTO.BidDTO(username != null ? username : "You", currentPrice, LocalDateTime.now().format(ISO_FMT)));
                        // Append a live point to the chart without clearing existing data
                        appendChartPoint(currentPrice);
                    } else {
                        showBidError(response != null ? response.getMessage() : "Lỗi kết nối máy chủ");
                    }
                })
            );
        } catch (ValidationException e) {
            showBidError(e.getMessage());
        }
    }

    private void handleEnableAutoBid() {
        try {
            bidService.setAutoBid(
                    this.auctionId,
                    autoBidMaxInputField.getText().trim(),
                    autoBidIncrementInputField.getText().trim(),
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

    /**
     * (Re)builds the chart from bidHistory.
     * Always keeps at most MAX_CHART_POINTS points.
     */
    private void loadChartData() {
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

    /**
     * Appends a single live price point captured at the moment currentPrice changes.
     * Automatically trims the oldest point when the count exceeds MAX_CHART_POINTS.
     */
    private void appendChartPoint(double price) {
        String label = LocalDateTime.now().format(CHART_FMT);
        chartSeries.getData().add(new XYChart.Data<>(label, price));
        while (chartSeries.getData().size() > MAX_CHART_POINTS) {
            chartSeries.getData().remove(0);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Countdown timer
    // ─────────────────────────────────────────────────────────────
    private void startCountdown() {
        if (countdownTimeline != null) countdownTimeline.stop();
        if (endTime == null) {
            endsInLabel.setText("N/A");
            return;
        }
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateCountdown()));
        countdownTimeline.setCycleCount(Animation.INDEFINITE);
        countdownTimeline.play();
        updateCountdown();
    }

    private void updateCountdown() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endTime)) {
            endsInLabel.setText("Ended");
            if (countdownTimeline != null) countdownTimeline.stop();
            // Fetch final details from server to populate winner
            com.auction.client.ClientContext.auctionService().getAuctionDetail(this.auctionId, response -> Platform.runLater(() -> {
                if (response != null && response.isSuccess() && response.getData() instanceof com.auction.share.DTO.AuctionDetailDTO detail) {
                    showWinnerInfo(detail);
                } else {
                    showWinnerInfo(null);
                }
            }));
            return;
        }
        long days    = ChronoUnit.DAYS.between(now, endTime);
        long hours   = ChronoUnit.HOURS.between(now, endTime) % 24;
        long minutes = ChronoUnit.MINUTES.between(now, endTime) % 60;
        long seconds = ChronoUnit.SECONDS.between(now, endTime) % 60;
        endsInLabel.setText(String.format("%dd %02dh %02dm %02ds", days, hours, minutes, seconds));
    }

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
            socketClient().removePushListener(bidPushListener);
        }
        bidPushListener = response -> {
            if (response != null
                    && response.isSuccess()
                    && "BID_UPDATED".equals(response.getMessage())
                    && response.getData() instanceof BidUpdateEvent event
                    && auctionId != null
                    && auctionId.equals(event.getAuctionId())) {
                Platform.runLater(this::refreshAuctionDetail);
            }
        };
        socketClient().addPushListener(bidPushListener);
    }

    private void refreshAuctionDetail() {
        ClientContext.auctionService().getAuctionDetail(this.auctionId, response -> Platform.runLater(() -> {
            if (response != null && response.isSuccess() && response.getData() instanceof AuctionDetailDTO detail) {
                this.currentPrice = detail.getCurrentPrice();
                this.bidStep = detail.getBidStep();
                this.totalBids = detail.getBidHistory() != null ? detail.getBidHistory().size() : this.totalBids;

                this.currentPriceLabel.setText(String.format("%.0f VND", this.currentPrice));
                this.totalBidsLabel.setText(String.valueOf(this.totalBids));
                this.minBidLabel.setText(String.format("Minimum bid: %.0f VND", this.currentPrice + this.bidStep));
                this.sellerNameLabel.setText(detail.getSellerName());
                this.descriptionLabel.setText(detail.getDescription() != null ? detail.getDescription() : "No description provided.");

                if (detail.getImageBytes() != null && detail.getImageBytes().length > 0) {
                    try {
                        javafx.scene.image.Image img = new javafx.scene.image.Image(new java.io.ByteArrayInputStream(detail.getImageBytes()));
                        javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
                        imgView.setFitWidth(150);
                        imgView.setFitHeight(150);
                        imgView.setPreserveRatio(true);
                        productIconLabel.setGraphic(imgView);
                        productIconLabel.setText(""); // clear emoji
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    productIconLabel.setGraphic(null);
                    productIconLabel.setText("📦");
                }

                this.bidHistory = detail.getBidHistory() != null ? detail.getBidHistory() : new java.util.ArrayList<>();
                this.startingPrice = detail.getStartingPrice();

                this.startTimeISO = detail.getStartTime();
                if (detail.getStartTime() != null) {
                    this.startTimeLabel.setText(detail.getStartTime());
                }
                if (detail.getEndTime() != null) {
                    this.endTimeLabel.setText(detail.getEndTime());
                    try {
                        this.endTime = LocalDateTime.parse(detail.getEndTime(), DISPLAY_FMT);
                    } catch (Exception ignored) {}
                }

                loadChartData();

                if ("FINISHED".equals(detail.getStatus()) || (this.endTime != null && LocalDateTime.now().isAfter(this.endTime))) {
                    showWinnerInfo(detail);
                }
            }
        }));
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
