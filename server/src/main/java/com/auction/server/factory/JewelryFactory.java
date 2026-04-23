package com.auction.server.controllers;

import com.auction.share.models.item.Item;
import com.auction.share.models.item.Jewelry;

public class JewelryFactory implements ItemCreator {
    @Override
    public Item createItem(String name, String description, double startingPrice, int quantity, String condition, String... attributes) {
        // Giá trị mặc định
        String material = "Unknown";
        double caratWeight = 0.0;


        // Bóc tách attributes:
        // attributes[0] -> material
        // attributes[1] -> caratWeight
        // attributes[2] -> gemstoneType

        if (attributes != null && attributes.length > 0) {
            material = attributes[0];
        }

        if (attributes != null && attributes.length > 1) {
            try {
                caratWeight = Double.parseDouble(attributes[1]);
            } catch (NumberFormatException e) {
                System.err.println("Lỗi định dạng khối lượng carat: " + attributes[1]);
                caratWeight = 0.0;
            }
        }


        // Tạo và trả về đối tượng Jewelry
        return new Jewelry(name, description, startingPrice, quantity, condition, material, caratWeight);
    }
}