package com.auction.share.models.item;

import com.auction.share.enums.Category;

public class Vehicle extends Item {
    private double mileage;
    private String fuelType;

    public Vehicle(String name, String description, double startingPrice, String sellerId ,
                    String fuelType) {
        super(name, description, startingPrice, sellerId, Category.VEHICLE);
        this.fuelType = fuelType;
    }

    public double getMileage() {
        return mileage;
    }
    public String getFuelType(){
        return fuelType;
    }
}

