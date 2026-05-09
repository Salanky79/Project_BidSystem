package com.auction.server.factory;

import com.auction.share.models.item.Item;
import com.auction.share.models.item.RealEstate;

public class RealEstateFactory implements ItemCreator {
    @Override
    public Item createItem(String name, String description, double startingPrice, String sellerID, String... attributes) {
        String location = getTextAttribute(attributes, 0, "Unknown Location");
        double area = parseDoubleAttribute(attributes, 1, 0.0);

        return new RealEstate(name, description, startingPrice, sellerID, location, area);
    }

    private static String getTextAttribute(String[] attributes, int index, String defaultValue) {
        if (attributes == null || attributes.length <= index || attributes[index] == null || attributes[index].isBlank()) {
            return defaultValue;
        }
        return attributes[index];
    }

    private static double parseDoubleAttribute(String[] attributes, int index, double defaultValue) {
        String value = getTextAttribute(attributes, index, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
