package com.auction.share.models.item;

import com.auction.share.enums.Category;

public class Vehicle extends Item {
    private double mileage;
    private String fuelType;

    public Vehicle(String name, String description, double startingPrice, int quantity,
                   String condition, double mileage, String fuelType) {
        super(name, description, startingPrice, quantity, condition);
        this.mileage = mileage;
        this.fuelType = fuelType;
        this.setCategory(Category.VEHICLE);
    }

    public double getMileage() {
        return mileage;
    }
    public String getFuelType() {
        return fuelType;
    }
}

