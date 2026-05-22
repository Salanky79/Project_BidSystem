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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
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
import javafx.stage.Stage;
import javafx.util.Duration;
import com.auction.client.service.WatchlistService;

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

    @FXML private Label sellerNameLabel;
    @FXML private Label ratingLabel;
    @FXML private Label descriptionLabel;

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

    // ── COMMENTS ─────────────────────────────────────────────────
    @FXML private ComboBox<String> commentSortBox;
    @FXML private ListView<String> commentsListView;
    @FXML private TextField        commentInputField;
    @FXML private Button           postCommentButton;

    // ── State ────────────────────────────────────────────────────
    private double        currentPrice;
    private double        bidStep;
    private int           totalBids;
    private String        auctionId;
    private LocalDateTime endTime;
    private Timeline      countdownTimeline;
    private boolean       isFollowing = false;
    private boolean       autoBidEnabled = false;

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
        enableAutoBidButton.setOnAction(e -> handleEnableAutoBid());
        cancelAutoBidButton.setOnAction(e -> handleCancelAutoBid());

        // Post Comment button
        postCommentButton.setOnAction(e -> handlePostComment());

        // Price-history filter buttons
        btnDay.setOnAction(e   -> loadChartData("day"));
        btnWeek.setOnAction(e  -> loadChartData("week"));
        btnMonth.setOnAction(e -> loadChartData("month"));

        // Default chart data
        loadChartData("month");

        // Custom cell factory for comments ListView
        commentsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }

                // Format stored as "author|text|time"
                String[] parts = item.split("\\|", 3);
                String author    = parts.length > 0 ? parts[0] : "?";
                String text      = parts.length > 1 ? parts[1] : item;
                String timestamp = parts.length > 2 ? parts[2] : "";

                // Initials avatar
                String initials = author.length() >= 2
                        ? (author.substring(0, 1) + author.substring(author.length() - 1)).toUpperCase()
                        : author.toUpperCase();

                Label avatarLabel = new Label(initials);
                avatarLabel.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold; -fx-font-size: 12px;");
                VBox avatar = new VBox(avatarLabel);
                avatar.setAlignment(Pos.CENTER);
                avatar.setStyle("-fx-min-width: 36; -fx-min-height: 36; -fx-max-width: 36; -fx-max-height: 36;"
                        + " -fx-background-color: #D4AF37; -fx-background-radius: 50;");

                // Author + timestamp row
                Label authorLabel = new Label(author);
                authorLabel.setStyle("-fx-text-fill: #D4AF37; -fx-font-weight: bold; -fx-font-size: 13px;");
                HBox.setHgrow(authorLabel, Priority.ALWAYS);

                Label timeLabel = new Label(timestamp);
                timeLabel.setStyle("-fx-text-fill: #777777; -fx-font-size: 11px;");

                HBox header = new HBox(authorLabel, timeLabel);
                header.setAlignment(Pos.CENTER_LEFT);
                header.setSpacing(8);

                Label textLabel = new Label(text);
                textLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 13px;");
                textLabel.setWrapText(true);

                VBox content = new VBox(header, textLabel);
                content.setSpacing(3);
                HBox.setHgrow(content, Priority.ALWAYS);

                HBox card = new HBox(avatar, content);
                card.setSpacing(12);
                card.setAlignment(Pos.TOP_LEFT);
                card.setPadding(new Insets(12, 14, 12, 14));
                card.setStyle("-fx-background-color: #161616; -fx-background-radius: 8;"
                        + " -fx-border-color: #D4AF37 transparent transparent transparent;"
                        + " -fx-border-width: 0 0 0 3;");

                setGraphic(card);
                setStyle("-fx-background-color: transparent; -fx-padding: 4 0 4 0;");
                setPrefWidth(0); // allow wrapping
            }
        });
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
        startTimeLabel.setText(LocalDateTime.now().format(DISPLAY_FMT));
        sellerNameLabel.setText("Unknown");
        descriptionLabel.setText("Loading description...");
        if (productIconLabel != null && icon != null) {
            productIconLabel.setText(icon);
        }
        minBidLabel.setText(String.format("Minimum bid: %.0f VND", price + bidStep));
        resetAutoBidState();

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
        
        // Sync initial follow state
        this.isFollowing = WatchlistService.getInstance().isFollowed(this.auctionId);
        updateFollowButtonStyle();

        // Fetch latest data from server
        com.auction.client.ClientContext.auctionService().getAuctionDetail(this.auctionId, response -> Platform.runLater(() -> {
            if (response != null && response.isSuccess() && response.getData() instanceof com.auction.share.DTO.AuctionDetailDTO detail) {
                this.currentPrice = detail.getCurrentPrice();
                this.bidStep = detail.getBidStep();
                this.totalBids = detail.getBidHistory() != null ? detail.getBidHistory().size() : this.totalBids;

                this.currentPriceLabel.setText(String.format("%.0f VND", this.currentPrice));
                this.totalBidsLabel.setText(String.valueOf(this.totalBids));
                this.minBidLabel.setText(String.format("Minimum bid: %.0f VND", this.currentPrice + this.bidStep));
                this.sellerNameLabel.setText(detail.getSellerName());
                this.descriptionLabel.setText(detail.getDescription() != null ? detail.getDescription() : "No description provided.");
            }
        }));
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
    private void handleFollowToggle() {
        isFollowing = !isFollowing;
        WatchlistService.getInstance().toggle(this.auctionId);
        updateFollowButtonStyle();
        
        // Refresh home if we are in Watchlist view? 
        // Not easily accessible here, but the data is updated.
    }
    
    private void updateFollowButtonStyle() {
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
                        minBidLabel.setText(String.format("Minimum bid: %.0f VND", currentPrice + bidStep));
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

    private void handlePostComment() {
        String text = commentInputField.getText().trim();
        if (text.isEmpty()) return;

        String timestamp = LocalDateTime.now().format(DISPLAY_FMT);
        // Store as "author|text|time" for the CellFactory to parse
        commentsListView.getItems().add(0, "You|" + text + "|" + timestamp);
        commentsListView.scrollTo(0);
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

    private void updateAutoBidControls() {
        enableAutoBidButton.setDisable(autoBidEnabled);
        cancelAutoBidButton.setDisable(!autoBidEnabled);
        autoBidMaxInputField.setDisable(autoBidEnabled);
        autoBidIncrementInputField.setDisable(autoBidEnabled);
    }
}
