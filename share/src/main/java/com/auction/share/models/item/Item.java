package com.auction.share.models.item;

import com.auction.share.models.core.Entity;

public class Item extends Entity {
    private String name;
    private String description = "";
    private double startingPrice;
    private String sellerId;

    public Item(String name, String description, double startingPrice, String sellerId) {
        super();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.sellerId = sellerId;
    }

    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public double getStartingPrice() {
        return startingPrice;
    }
    public String getSellerId() {
        return sellerId;
    }
}

