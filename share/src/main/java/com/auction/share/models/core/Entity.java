package com.auction.share.models.core;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lớp cơ sở cho các entity của hệ thống.
 */
public abstract class Entity {
    private String id;
    private LocalDateTime createdAt;

    public Entity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }
}