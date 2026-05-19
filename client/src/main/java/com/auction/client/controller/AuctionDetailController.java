package com.auction.client.controller;

import com.auction.client.ClientContext;
import com.auction.client.service.BidService;
import com.auction.share.exceptions.ValidationException;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class AuctionDetailController {

    private final BidService bidService = ClientContext.bidService();

    // ── HEADER ──────────────────────────────────────────────────
    @FXML private Label  productTitleLabel;
    @FXML private Button closeButton;
    @FXML private Button followButton;

    // ── LEFT COLUMN ─────────────────────────────────────────────
    @FXML private Button         btnDay;
    @FXML private Button         btnWeek;
    @FXML private Button         btnMonth;
    @FXML private LineChart<String, Number> priceHistoryChart;
    @FXML private CategoryAxis   chartXAxis;
    @FXML private NumberAxis     chartYAxis;

    @FXML private Label sellerNameLabel;
    @FXML private Label ratingLabel;
    @FXML private Label descriptionLabel;

    // ── RIGHT COLUMN ─────────────────────────────────────────────
    @FXML private Label     currentPriceLabel;
    @FXML private Label     totalBidsLabel;
    @FXML private Label     startTimeLabel;
    @FXML private Label     endTimeLabel;
    @FXML private Label     endsInLabel;
    @FXML private ImageView productImageView;
    @FXML private TextField bidInputField;
    @FXML private Button    placeBidButton;
    @FXML private Label     minBidLabel;

    // ── COMMENTS ─────────────────────────────────────────────────
    @FXML private ComboBox<String> commentSortBox;
    @FXML private ListView<String> commentsListView;
    @FXML private TextField        commentInputField;
    @FXML private Button           postCommentButton;

    // ── State ────────────────────────────────────────────────────
    private double        currentPrice;
    private int           totalBids;
    private String        auctionId;
    private LocalDateTime endTime;
    private Timeline      countdownTimeline;
    private boolean       isFollowing = false;

    private static final String FOLLOW_GOLD_STYLE =
            "-fx-background-color: transparent; -fx-border-color: #D4AF37; -fx-border-radius: 20; -fx-background-radius: 20; -fx-text-fill: #D4AF37; -fx-font-size: 12px; -fx-padding: 5 16 5 16; -fx-cursor: hand;";
    private static final String FOLLOW_GRAY_STYLE =
            "-fx-background-color: transparent; -fx-border-color: #777777; -fx-border-radius: 20; -fx-background-radius: 20; -fx-text-fill: #888888; -fx-font-size: 12px; -fx-padding: 5 16 5 16; -fx-cursor: hand;";

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter ISO_FMT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        // Close button
        closeButton.setOnAction(e -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });

        // Follow toggle
        followButton.setOnAction(e -> handleFollowToggle());

        // Place Bid button
        placeBidButton.setOnAction(e -> handlePlaceBid());

        // Post Comment button
        postCommentButton.setOnAction(e -> handlePostComment());

        // Price-history filter buttons
        btnDay.setOnAction(e   -> loadChartData("day"));
        btnWeek.setOnAction(e  -> loadChartData("week"));
        btnMonth.setOnAction(e -> loadChartData("month"));

        // Default chart data
        loadChartData("month");
    }

    // ─────────────────────────────────────────────────────────────
    // Public API – called by the opener (ItemCardController etc.)
    // ─────────────────────────────────────────────────────────────
    public void setData(
            String icon,
            String category,
            String name,
            double price,
            int    bids,
            String time,
            String status,
            String auctionId
    ) {
        this.currentPrice = price;
        this.totalBids    = bids;
        this.auctionId    = auctionId;

        productTitleLabel.setText(name);
        currentPriceLabel.setText(String.format("%.0f VND", price));
        totalBidsLabel.setText(String.valueOf(bids));
        endTimeLabel.setText(time);
        startTimeLabel.setText(LocalDateTime.now().format(DISPLAY_FMT));
        sellerNameLabel.setText("Unknown");
        descriptionLabel.setText("No description provided.");
        minBidLabel.setText(String.format("Minimum bid: %.0f VND", price + 500));

        // Parse end time for countdown
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
    }

    public void setData(
            String icon,
            String category,
            String name,
            double price,
            int    bids,
            String startDate,
            String endDate,
            String listedBy,
            String description,
            String status
    ) {
        this.currentPrice = price;
        this.totalBids    = bids;

        productTitleLabel.setText(name);
        currentPriceLabel.setText(String.format("%.0f VND", price));
        totalBidsLabel.setText(String.valueOf(bids));
        startTimeLabel.setText(startDate);
        endTimeLabel.setText(endDate);
        sellerNameLabel.setText(listedBy);
        descriptionLabel.setText(description);
        minBidLabel.setText(String.format("Minimum bid: %.0f VND", price + 500));

        // Parse end time for countdown
        try {
            this.endTime = LocalDateTime.parse(endDate, ISO_FMT);
        } catch (Exception ex) {
            try {
                this.endTime = LocalDateTime.parse(endDate, DISPLAY_FMT);
            } catch (Exception ignored) {
                this.endTime = null;
            }
        }
        startCountdown();
    }

    // ─────────────────────────────────────────────────────────────
    // Static factory – opens a new Stage with AuctionDetailv2.fxml
    // ─────────────────────────────────────────────────────────────
    public static void open(
            String icon,
            String category,
            String name,
            double price,
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
            ctrl.setData(icon, category, name, price, bids, time, status, auctionId);

            Stage stage = new Stage();
            stage.setTitle("Auction – " + name);
            stage.setScene(new Scene(root, 900, 650));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading AuctionDetailv2.fxml");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Handlers
    // ─────────────────────────────────────────────────────────────
    private void handleFollowToggle() {
        isFollowing = !isFollowing;
        if (isFollowing) {
            followButton.setText("− Unfollow");
            followButton.setStyle(FOLLOW_GRAY_STYLE);
        } else {
            followButton.setText("+ Follow");
            followButton.setStyle(FOLLOW_GOLD_STYLE);
        }
    }

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
                        minBidLabel.setText(String.format("Minimum bid: %.0f VND", currentPrice + 500));
                        bidInputField.clear();
                    } else {
                        showBidError(response != null ? response.getMessage() : "Lỗi kết nối máy chủ");
                    }
                })
            );
        } catch (ValidationException e) {
            showBidError(e.getMessage());
        }
    }

    private void handlePostComment() {
        String text = commentInputField.getText().trim();
        if (text.isEmpty()) return;
        commentsListView.getItems().add(0, "You: " + text);
        commentInputField.clear();
    }

    // ─────────────────────────────────────────────────────────────
    // Price-history chart
    // ─────────────────────────────────────────────────────────────
    private void loadChartData(String range) {
        priceHistoryChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        switch (range) {
            case "day" -> {
                series.getData().add(new XYChart.Data<>("08:00", currentPrice * 0.90));
                series.getData().add(new XYChart.Data<>("10:00", currentPrice * 0.93));
                series.getData().add(new XYChart.Data<>("12:00", currentPrice * 0.95));
                series.getData().add(new XYChart.Data<>("14:00", currentPrice * 0.97));
                series.getData().add(new XYChart.Data<>("16:00", currentPrice * 0.99));
                series.getData().add(new XYChart.Data<>("Now",   currentPrice));
            }
            case "week" -> {
                series.getData().add(new XYChart.Data<>("Mon", currentPrice * 0.75));
                series.getData().add(new XYChart.Data<>("Tue", currentPrice * 0.80));
                series.getData().add(new XYChart.Data<>("Wed", currentPrice * 0.85));
                series.getData().add(new XYChart.Data<>("Thu", currentPrice * 0.88));
                series.getData().add(new XYChart.Data<>("Fri", currentPrice * 0.92));
                series.getData().add(new XYChart.Data<>("Sat", currentPrice * 0.97));
                series.getData().add(new XYChart.Data<>("Now", currentPrice));
            }
            default -> {  // month
                series.getData().add(new XYChart.Data<>("W1", currentPrice * 0.60));
                series.getData().add(new XYChart.Data<>("W2", currentPrice * 0.72));
                series.getData().add(new XYChart.Data<>("W3", currentPrice * 0.85));
                series.getData().add(new XYChart.Data<>("W4", currentPrice * 0.94));
                series.getData().add(new XYChart.Data<>("Now", currentPrice));
            }
        }

        priceHistoryChart.getData().add(series);
        highlightActiveFilter(range);
    }

    private void highlightActiveFilter(String active) {
        String gold   = "-fx-background-color: #FFD700; -fx-border-color: #FFD700; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #000000; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 14 5 14; -fx-cursor: hand;";
        String normal = "-fx-background-color: #222222; -fx-border-color: #3A3A3A; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #AAAAAA; -fx-font-size: 11px; -fx-padding: 5 14 5 14; -fx-cursor: hand;";
        btnDay.setStyle(active.equals("day")   ? gold : normal);
        btnWeek.setStyle(active.equals("week") ? gold : normal);
        btnMonth.setStyle(active.equals("month") ? gold : normal);
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
}
