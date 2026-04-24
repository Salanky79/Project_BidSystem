package com.auction.share.models.item;

public class Antique extends Item {
    private String era;
    private String material;

    public Antique(String name, String description, double startingPrice, int i, String sellerID , String era,
                   String material, String sellerId) {
        super(name, description, startingPrice, sellerId);
        this.era = era;
        this.material = material;
    }

    public String getEra() {
        return era;
    }
    public String getMaterial() {
        return material;
    }
}

