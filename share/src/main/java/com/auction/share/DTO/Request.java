package com.auction.share.DTO;

import java.io.Serializable;
import java.util.UUID;

// Serializable để truyền request qua socket/stream.
/**
 * Khung request chung cho các hành động gửi lên Server.
 */
public abstract class Request implements Serializable {
    private String requestId = UUID.randomUUID().toString();
    private final String action;

    protected Request(String action) {
        this.action = action;
    }

    /**
     * Gắn userId vào request khi Server cần chuẩn hóa theo session.
     */
    public Request withUserId(String userId) {
        return this;
    }

    public String getAction() {
        return action;
    }

    public String getRequestId() {
        return requestId;
    }
}