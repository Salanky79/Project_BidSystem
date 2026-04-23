package com.auction.server.controllers;

import com.auction.share.models.item.Item;
import com.auction.share.models.item.RealEstate;

public class RealEstateFactory implements ItemCreator {
    @Override
    public Item createItem(String name, String description, double startingPrice, String sellerID, String... attributes) {
        String location = (attributes.length > 0) ? attributes[0] : "Chưa xác định";
        double area = 0.0;

        if (attributes.length > 1) {
            try {
                area = Double.parseDouble(attributes[1]);
            } catch (NumberFormatException e) {
                area = 0.0;
            }
        }

        return new RealEstate(name, description, startingPrice, sellerID, location, area);
    }
}