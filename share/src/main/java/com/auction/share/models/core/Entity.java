package com.auction.share.models.core;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Entity {
    private String id;
    private LocalDateTime createdAt;

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

