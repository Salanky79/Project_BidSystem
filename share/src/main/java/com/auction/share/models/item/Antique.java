package com.auction.share.models.item;

import com.auction.share.enums.Category;

public class Antique extends Item {
    private String era;
    private String material;

    public Antique(String name, String description, double startingPrice, int quantity,
                   String condition, String era,
                   String material, String sellerId) {
        super(name, description, startingPrice, quantity, condition, sellerId);
        this.era = era;
        this.material = material;
        this.setCategory(Category.ANTIQUE);
    }

    public String getEra() {
        return era;
    }
    public String getMaterial() {
        return material;
    }
}

