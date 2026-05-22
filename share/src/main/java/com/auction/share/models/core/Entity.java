package com.auction.share.models.core;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lớp cơ sở cho các model có định danh và thời gian tạo.
 */
public abstract class Entity {
    private String id;
    private LocalDateTime createdAt;

    /**
     * Tạo mới với UUID và thời gian hiện tại.
     */
    public Entity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }
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