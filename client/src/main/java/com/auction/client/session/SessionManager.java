package com.auction.client.session;

/**
 * Lưu trạng thái phiên đăng nhập trên client.
 */
public class SessionManager {
    private volatile String currentUserId;

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void clear() {
        this.currentUserId = null;
    }
}

