package com.auction.share.models.item;

public class RealEstate extends Item {
    private String location;
    private double areaSquareMeter;

    public RealEstate(String name, String description, double startingPrice, String sellerId,
                      String location, double areaSquareMeter) {
        super(name, description, startingPrice, sellerId);
        this.location = location;
        this.areaSquareMeter = areaSquareMeter;
    }

    public String getLocation() {
        return location;
    }
    public double getAreaSquareMeter() {
        return areaSquareMeter;
    }
}

