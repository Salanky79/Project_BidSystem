package com.auction.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ItemCardController {

    @FXML private Label iconLabel;
    @FXML private Label categoryLabel;
    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label bidsLabel;
    @FXML private Label timeLabel;

    // Hàm này sẽ được HomeController gọi để truyền dữ liệu vào
    public void setData(String icon, String category, String name, double price, int bids, String time) {
        iconLabel.setText(icon);
        categoryLabel.setText(category);
        nameLabel.setText(name);

        // Format số tiền cho đẹp (vd: 1000.0 -> "1,000 USD")
        priceLabel.setText(String.format("%,.0f USD", price));

        // Xử lý chữ "bid" hay "bids"
        bidsLabel.setText(bids + (bids <= 1 ? " bid" : " bids"));
        timeLabel.setText("Ending In : " + time);
    }
}