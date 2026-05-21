package com.auction.client.session;

public class SessionManager {
    private volatile String currentUserId;

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }
    // lưu ID

    public void clear() {
        this.currentUserId = null;
    }
}

