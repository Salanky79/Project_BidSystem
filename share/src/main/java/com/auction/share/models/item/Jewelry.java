package com.auction.share.models.item;

public class Jewelry extends Item {
    private String material;
    private double caratWeight;

    public Jewelry(String name, String description, double startingPrice,  String sellerID, String material, double caratWeight) {
        super(name, description, startingPrice, sellerID);
        this.material = material;
        this.caratWeight = caratWeight;
    }

    public String getMaterial() {
        return material;
    }
    public double getCaratWeight() {
        return caratWeight;
    }
}

