package com.auction.server.factory;

import com.auction.share.models.item.Electronic;
import com.auction.share.models.item.Item;

/**
 * Factory tạo Item đồ điện tử.
 */
public class ElectronicFactory implements ItemCreator {
    @Override
    public Item createItem(String name, String description, double startingPrice, String sellerID, String... attributes) {
        String brand = getTextAttribute(attributes, 0, "Unknown");
        int warrantyMonths = parseIntAttribute(attributes, 1, 0);

        return new Electronic(name, description, startingPrice, sellerID, brand, warrantyMonths);
    }

    private static String getTextAttribute(String[] attributes, int index, String defaultValue) {
        if (attributes == null || attributes.length <= index || attributes[index] == null || attributes[index].isBlank()) {
            return defaultValue;
        }
        return attributes[index];
    }

    private static int parseIntAttribute(String[] attributes, int index, int defaultValue) {
        String value = getTextAttribute(attributes, index, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
