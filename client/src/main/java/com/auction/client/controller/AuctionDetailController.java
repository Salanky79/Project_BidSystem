package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuctionDetailController {

    @FXML private Button closeBtn;
    @FXML private Button     placeBidBtn;
    @FXML private Button     postCommentBtn;

    @FXML private Label      itemImagePlaceholder;  // icon emoji
    @FXML private Label      listedByLabel;
    @FXML private Label descriptionLabel;

    @FXML private Label      itemTitleLabel;
    @FXML private Label      statusLabel;
    @FXML private Label      categoryLabel;

    @FXML private Label      currentPriceLabel;
    @FXML private Label      totalBidsLabel;
    @FXML private Label      startDateLabel;
    @FXML private Label      endDateLabel;

    @FXML private TextField bidField;
    @FXML private Label      validationLabel;

    @FXML private VBox commentList;
    @FXML private ScrollPane commentScrollPane;
    @FXML private TextField  commentField;

    private double currentPrice;
    private int    totalBids;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ──────────────────────────────────────────────────────
    //  setData() cơ bản – gọi từ ItemCardController (khớp HomeController)
    // ──────────────────────────────────────────────────────
    public void setData(String icon,
                        String category,
                        String name,
                        double price,
                        int    bids,
                        String time,
                        String status
    ) {
        this.currentPrice = price;
        this.totalBids    = bids;

        itemImagePlaceholder.setText(icon);
        categoryLabel.setText(category);
        itemTitleLabel.setText(name);
        currentPriceLabel.setText(String.format("$%.2f", price));
        totalBidsLabel.setText(String.valueOf(bids));
        endDateLabel.setText(time);
        statusLabel.setText(status);

        // Mặc định – thay bằng dữ liệu thật từ server khi có
        listedByLabel.setText("Unknown");
        descriptionLabel.setText("No description provided.");
        startDateLabel.setText(LocalDateTime.now().format(FORMATTER));
        statusLabel.setText("Active");
    }

    // ──────────────────────────────────────────────────────
    //  setData() đầy đủ – dùng khi server trả về đủ trường
    // ──────────────────────────────────────────────────────
    public void setData(String icon,
                        String category,
                        String name,
                        double price,
                        int    bids,
                        String startDate,
                        String endDate,
                        String listedBy,
                        String description,
                        String status) {
        this.currentPrice = price;
        this.totalBids    = bids;

        itemImagePlaceholder.setText(icon);
        categoryLabel.setText(category);
        itemTitleLabel.setText(name);
        currentPriceLabel.setText(String.format("$%.2f", price));
        totalBidsLabel.setText(String.valueOf(bids));
        startDateLabel.setText(startDate);
        endDateLabel.setText(endDate);
        listedByLabel.setText(listedBy);
        descriptionLabel.setText(description);
        statusLabel.setText(status);
    }

    @FXML
    public void initialize() {
        validationLabel.setVisible(false);
        placeBidBtn.setOnAction(e -> handlePlaceBid());
        // closeBtn  → onAction="#handleClose"   (đã khai báo trong FXML)
        // postCommentBtn → onAction="#handlePostComment" (đã khai báo trong FXML)
    }

    // Hàm để đóng popup khi ấn nút "Close" hoặc click ra ngoài
    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handlePlaceBid() {
        String input = bidField.getText().trim();

        if (input.isEmpty()) {
            showError("Vui lòng nhập số tiền muốn đặt.");
            return;
        }

        try {
            double bidAmount = Double.parseDouble(input);

            if (bidAmount <= currentPrice) {
                showError("Giá đặt phải cao hơn giá hiện tại: "
                        + String.format("$%.2f", currentPrice));
                return;
            }

            // TODO: Gửi bid lên Server
            System.out.println("Đặt giá: $" + bidAmount);

            // Cập nhật UI tạm thời (server push về sau)
            currentPrice = bidAmount;
            totalBids++;
            currentPriceLabel.setText(String.format("$%.2f", currentPrice));
            totalBidsLabel.setText(String.valueOf(totalBids));
            bidField.clear();
            hideValidation();

        } catch (NumberFormatException e) {
            showError("Số tiền không hợp lệ. Vui lòng nhập số.");
        }
    }

    @FXML
    private void handlePostComment() {
        String text = commentField.getText().trim();
        if (text.isEmpty()) return;

        // Tạo comment row mới, thêm vào cuối commentList
        VBox row = buildCommentRow("Bạn", text,
                LocalDateTime.now().format(FORMATTER));
        commentList.getChildren().add(row);

        commentField.clear();

        // TODO: Gửi comment lên server

        // Cuộn xuống cuối danh sách
        commentScrollPane.layout();
        commentScrollPane.setVvalue(1.0);
    }

    private VBox buildCommentRow(String author, String text, String timestamp) {
        Label authorLabel = new Label(author);
        authorLabel.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #c9a84c;");

        Label timeLabel = new Label(timestamp);
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(authorLabel, spacer, timeLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        Label textLabel = new Label(text);
        textLabel.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-wrap-text: true;");
        textLabel.setMaxWidth(500);

        VBox row = new VBox(header, textLabel);
        row.setStyle(
                "-fx-padding: 12 16 12 16; " +
                        "-fx-border-color: #333333; " +
                        "-fx-border-width: 0 0 1 0;");
        return row;
    }

    // ──────────────── Validation helpers ────────────────

    private void showError(String message) {
        validationLabel.setText(message);
        validationLabel.setVisible(true);
    }

    private void hideValidation() {
        validationLabel.setText("");
        validationLabel.setVisible(false);
    }

    // ──────────────────────────────────────────────────────
    //  STATIC FACTORY – Được gọi từ ItemCardController
    //  AuctionDetailController.open(icon, category, name, price, bids, timeLeft);
    // ──────────────────────────────────────────────────────
    public static void open(String icon, String category, String name,
                            double price, int bids, String time, String status) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    AuctionDetailController.class.getResource(
                            "/com/auction/client/view/AuctionDetail.fxml")
            );
            Parent root = loader.load();

            AuctionDetailController controller = loader.getController();
            controller.setData(icon, category, name, price, bids, time, status);

            Stage stage = new Stage();
            stage.setTitle("Auction – " + name);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi: Không tìm thấy AuctionDetail.fxml!");
        }
    }
}