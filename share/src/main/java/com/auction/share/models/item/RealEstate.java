package com.auction.share.models.item;

import com.auction.share.enums.Category;

public class RealEstate extends Item {
    private String location;
    private double areaSquareMeter;

    public RealEstate(String name, String description, double startingPrice, int quantity,
                      String condition, String location, double areaSquareMeter) {
        super(name, description, startingPrice, quantity, condition);
        this.location = location;
        this.areaSquareMeter = areaSquareMeter;
        this.setCategory(Category.REAL_ESTATE);
    }


    public String getLocation() {
        return location;
    }
    public double getAreaSquareMeter() {
        return areaSquareMeter;
    }

}

