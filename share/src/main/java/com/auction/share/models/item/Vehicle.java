package com.auction.share.models.item;

public class Vehicle extends Item {
    private double mileage;
    private String fuelType;

    public Vehicle(String name, String description, double startingPrice, String sellerId ,
                    double mileage, String fuelType) {
        super(name, description, startingPrice, sellerId);
        this.mileage = mileage;
        this.fuelType = fuelType;
    }

    public double getMileage() {
        return mileage;
    }
    public String getFuelType(){
        return fuelType;
    }
}

