package com.auction.server.factory;

import com.auction.share.models.item.Item;
import com.auction.share.models.item.Vehicle;

/**
 * Factory tạo Item xe cộ.
 */
public class VehicleFactory implements ItemCreator {
    @Override
    public Item createItem(String name, String description, double startingPrice, String sellerID, String... attributes) {
        double mileage = parseDoubleAttribute(attributes, 0, 0.0);
        String fuelType = getTextAttribute(attributes, 1, "Unknown Fuel");

        return new Vehicle(name, description, startingPrice, sellerID, fuelType);
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
