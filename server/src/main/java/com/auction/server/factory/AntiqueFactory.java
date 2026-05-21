package com.auction.server.factory;

import com.auction.share.models.item.Antique;
import com.auction.share.models.item.Item;

public class AntiqueFactory implements ItemCreator {
    @Override
    public Item createItem(String name, String description, double startingPrice, String sellerID, String... attributes) {
        String era = "Unknown Era";
        String material = "Unknown Material";

        if (attributes != null && attributes.length > 0 && attributes[0] != null && !attributes[0].isBlank()) {
            era = attributes[0];
        }
        if (attributes != null && attributes.length > 1 && attributes[1] != null && !attributes[1].isBlank()) {
            material = attributes[1];
        }

        return new Antique(name, description, startingPrice, sellerID, era, material);
    }
}
