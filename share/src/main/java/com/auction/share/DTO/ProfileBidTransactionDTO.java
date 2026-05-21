package com.auction.share.DTO;

import java.io.Serializable;

/**
 * DTO tóm tắt lịch sử đặt giá trong profile.
 */
public class ProfileBidTransactionDTO implements Serializable {
    // serialVersionUID để giữ tương thích khi serialize/deserialize.
    private static final long serialVersionUID = 1L;

    private final String itemName;
    private final String status;
    private final double bidAmount;
    // Dùng String để tránh sai lệch múi giờ khi parse ở client.
    private final String timestamp;

    public ProfileBidTransactionDTO(String itemName, String status, double bidAmount, String timestamp) {
        this.itemName = itemName;
        this.status = status;
        this.bidAmount = bidAmount;
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }
}
