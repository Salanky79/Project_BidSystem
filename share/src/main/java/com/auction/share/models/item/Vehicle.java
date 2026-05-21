package com.auction.share.models.item;

import com.auction.share.enums.Category;

/**
 * Sản phẩm danh mục xe cộ.
 */
public class Vehicle extends Item {
    private String fuelType;

    public Vehicle(String name, String description, double startingPrice, String sellerId,
                   String fuelType) {
        super(name, description, startingPrice, sellerId, Category.VEHICLE);
        this.fuelType = fuelType;
    }

    public String getFuelType() {
        return fuelType;
    }
}