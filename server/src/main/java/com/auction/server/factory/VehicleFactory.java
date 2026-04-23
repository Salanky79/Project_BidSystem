package com.auction.server.factory;

import com.auction.share.models.item.Item;
import com.auction.share.models.item.Vehicle;

public class VehicleFactory implements ItemCreator {
    @Override
    public Item createItem(String name, String description, double startingPrice, int quantity, String condition, String... attributes) {
        // Giá trị mặc định
        double mileage = 0.0;
        String fuelType = "Chưa xác định";

        // attributes[0] -> mileage (Số km)
        // attributes[1] -> fuelType (Xăng/Dầu/Điện)

        if (attributes != null && attributes.length > 0) {
            try {
                mileage = Double.parseDouble(attributes[0]);
            } catch (NumberFormatException e) {
                mileage = 0.0;
            }
        }

        if (attributes != null && attributes.length > 1) {
            fuelType = attributes[1];
        }

        return new Vehicle(name, description, startingPrice, quantity, condition, mileage, fuelType);
    }
}