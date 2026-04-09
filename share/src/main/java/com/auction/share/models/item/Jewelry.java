package com.auction.share.models.item;

import com.auction.share.enums.Category;

public class Jewelry extends Item {
    private String material;
    private double caratWeight;
    private String gemstoneType;

    public Jewelry(String name, String description, double startingPrice, int quantity,
                   String condition, String material,
                   double caratWeight, String gemstoneType) {
        super(name, description, startingPrice, quantity, condition);
        this.material = material;
        this.caratWeight = caratWeight;
        this.gemstoneType = gemstoneType;
        this.setCategory(Category.JEWELRY);
    }

    public String getMaterial() {
        return material;
    }
    public double getCaratWeight() {
        return caratWeight;
    }
    public String getGemstoneType() {
        return gemstoneType;
    }
}

