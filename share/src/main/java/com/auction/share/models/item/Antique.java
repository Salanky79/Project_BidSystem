package com.auction.share.models.item;

import com.auction.share.enums.Category;

public class Antique extends Item {
    private String era;
    private String material;
    private boolean hasAuthenticityCert;

    public Antique(String name, String description, double startingPrice, int quantity,
                   String condition, String era,
                   String material, boolean hasAuthenticityCert) {
        super(name, description, startingPrice, quantity, condition);
        this.era = era;
        this.material = material;
        this.hasAuthenticityCert = hasAuthenticityCert;
        this.setCategory(Category.ANTIQUE);
    }

    public String getEra() {
        return era;
    }
    public String getMaterial() {
        return material;
    }
    public boolean isHasAuthenticityCert() {
        return hasAuthenticityCert;
    }
}

