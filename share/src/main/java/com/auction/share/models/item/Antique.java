package com.auction.share.models.item;

import com.auction.share.enums.Category;

public class Antique extends Item {
    private String era;
    private String material;

    public Antique(String name, String description, double startingPrice, String sellerId, String era, String material) {
        super(name, description, startingPrice, sellerId, Category.ANTIQUE);
        this.era = era;
        this.material = material;
    }

    public String getEra() {
        return era;
    }

    public String getMaterial() {
        return material;
    }
}
