package com.auction.server.controllers;

import com.auction.share.models.item.Electronic;
import com.auction.share.models.item.Item;

public class ElectronicFactory implements ItemCreator {
    @Override
    public Item createItem(String name, String description, double startingPrice, int quantity, String condition, String... attributes) {
        // Giả sử: attributes[0] là brand, attributes[1] là warrantyMonths
        String brand = (attributes.length > 0) ? attributes[0] : "Unknown";

        int warrantyMonths = 0;
        if (attributes.length > 1) {
            try {
                warrantyMonths = Integer.parseInt(attributes[1]);
            } catch (NumberFormatException e) {
                warrantyMonths = 0; // Giá trị mặc định nếu lỗi định dạng
            }
        }

        return new Electronic(name, description, startingPrice, quantity, condition, brand, warrantyMonths);
    }
}