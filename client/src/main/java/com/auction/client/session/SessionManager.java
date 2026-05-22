package com.auction.client.session;

/**
 * Quản lý phiên làm việc của người dùng hiện tại ở phía client.
 */
public class SessionManager {
    // đảm bảo các luồng luôn đọc giá trị mới nhất từ main memory
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