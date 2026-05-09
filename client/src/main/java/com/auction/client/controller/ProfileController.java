package com.auction.client.controller; // Bạn hãy đổi lại đúng package của mình

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller cho màn hình hồ sơ cá nhân.
 * Được thiết kế để chạy mượt mà với JavaFX chuẩn và Scene Builder.
 */
public class ProfileController implements Initializable {

    // --- Các thành phần UI khớp với fx:id trong file FXML mới ---
    @FXML private Label nameLabel;      // Nhãn tên lớn phía trên [cite: 1]
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Button editButton;

    // --- TableView Lịch sử đấu giá ---
    @FXML private TableView<AuctionHistory> table;
    @FXML private TableColumn<AuctionHistory, String> colItem;
    @FXML private TableColumn<AuctionHistory, String> colStatus;
    @FXML private TableColumn<AuctionHistory, Double> colBid;
    @FXML private TableColumn<AuctionHistory, String> colTime;

    private boolean isEditing = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Nạp dữ liệu giả lập cho người dùng
        loadMockUserData();

        // 2. Cấu hình bảng lịch sử
        setupTableColumns();
        loadMockTableData();

        // 3. Xử lý sự kiện chỉnh sửa
        editButton.setOnAction(event -> handleEditAction());

        // Thiết lập trạng thái ban đầu: chỉ đọc
        setFieldsEditable(false);
    }

    /**
     * Giả lập dữ liệu người dùng thay cho Database
     */
    private void loadMockUserData() {
        nameLabel.setText("Nguyen Thai Son");
        nameField.setText("Nguyen Thai Son");
        usernameField.setText("songay.femboy");
        emailField.setText("songay@gmail.com");
        phoneField.setText("0123.456.789");
    }

    /**
     * Cấu hình các cột của TableView
     */
    private void setupTableColumns() {
        colItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colBid.setCellValueFactory(new PropertyValueFactory<>("bidAmount"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
    }

    /**
     * Giả lập dữ liệu lịch sử đấu giá
     */
    private void loadMockTableData() {
        ObservableList<AuctionHistory> history = FXCollections.observableArrayList(
                new AuctionHistory("Rolex Submariner Watch", "Won", 15500.0, "12/04/2026"),
                new AuctionHistory("Porsche 911 Supercar", "Running", 140000.0, "14/04/2026"),
                new AuctionHistory("24k Diamond Ring", "Lost", 12000.0, "10/04/2026")
        );
        table.setItems(history);
    }

    /**
     * Xử lý logic khi bấm nút Edit/Save
     */
    private void handleEditAction() {
        if (!isEditing) {
            // Chuyển sang chế độ cho phép sửa
            isEditing = true;
            setFieldsEditable(true);
            editButton.setText("SAVE CHANGES");
            editButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            // Thực hiện "Lưu" (Cập nhật giao diện)
            nameLabel.setText(nameField.getText());
            isEditing = false;
            setFieldsEditable(false);
            editButton.setText("Edit Profile");
            editButton.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white;");

            // Thông báo nhỏ
            System.out.println("Profile updated successfully!");
        }
    }

    /**
     * Bật/Tắt chế độ chỉnh sửa của các TextField
     */
    private void setFieldsEditable(boolean canEdit) {
        nameField.setEditable(canEdit);
        emailField.setEditable(canEdit);
        phoneField.setEditable(canEdit);

        // Thay đổi visual để người dùng nhận biết
        String style = canEdit ? "-fx-background-color: white; -fx-border-color: #3498db;"
                : "-fx-background-color: #f0f0f0; -fx-border-color: transparent;";
        nameField.setStyle(style);
        emailField.setStyle(style);
        phoneField.setStyle(style);
    }

    // --- Inner Class để làm Model cho TableView (Tạm thời) ---
    public static class AuctionHistory {
        private final String itemName;
        private final String status;
        private final double bidAmount;
        private final String timestamp;

        public AuctionHistory(String item, String status, double bid, String time) {
            this.itemName = item;
            this.status = status;
            this.bidAmount = bid;
            this.timestamp = time;
        }

        public String getItemName() { return itemName; }
        public String getStatus() { return status; }
        public double getBidAmount() { return bidAmount; }
        public String getTimestamp() { return timestamp; }
    }
}