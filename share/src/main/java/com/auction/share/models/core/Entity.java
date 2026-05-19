package com.auction.share.models.core;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lớp cơ sở (Base Class) cho tất cả các đối tượng Model trong hệ thống.
 * Cung cấp định danh duy nhất (UUID) và thời gian tạo mặc định cho mọi thực thể.
 */
public abstract class Entity {
    /**
     * Mã định danh duy nhất cho thực thể (định dạng UUID).
     */
    private String id;

    /**
     * Thời điểm thực thể được tạo ra.
     */
    private LocalDateTime createdAt;

    /**
     * Khởi tạo một đối tượng Entity mới.
     * Tự động sinh UUID và gán thời gian hiện tại cho thuộc tính createdAt.
     */
    public Entity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public String getId() {
        return id;
    }
    public void setID(String id){
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}