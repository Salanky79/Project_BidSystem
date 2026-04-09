package com.auction.share.models.item;

import com.auction.share.enums.Category;

public class RealEstate extends Item {
    private String propertyType;
    private String location;
    private double areaSquareMeter;
    private String legalStatus;

    public RealEstate(String name, String description, double startingPrice, int quantity,
                      String condition, String propertyType,
                      String location, double areaSquareMeter, String legalStatus) {
        super(name, description, startingPrice, quantity, condition);
        this.propertyType = propertyType;
        this.location = location;
        this.areaSquareMeter = areaSquareMeter;
        this.legalStatus = legalStatus;
        this.setCategory(Category.REAL_ESTATE);
    }

    public String getPropertyType() {
        return propertyType;
    }
    public String getLocation() {
        return location;
    }
    public double getAreaSquareMeter() {
        return areaSquareMeter;
    }
    public String getLegalStatus() {
        return legalStatus;
    }
}

