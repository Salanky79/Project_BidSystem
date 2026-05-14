package com.auction.share.models.item;

import com.auction.share.enums.Category;

public class Electronic extends Item {
    private String brand;
    private int warrantyMonths;

    public Electronic(String name, String description, double startingPrice, String sellerId, String brand, int warrantyMonths) {
        super(name, description, startingPrice, sellerId, Category.ELECTRONIC);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    public String getBrand() {
        return brand;
    }
    public int getWarrantyMonths() {
        return warrantyMonths;
    }
}

