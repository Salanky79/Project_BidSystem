package com.auction.server.controllers;

import com.auction.share.models.item.Art;
import com.auction.share.models.item.Item;

public class ArtFactory implements ItemCreator {
    @Override
    public Item createItem(String name, String description, double startingPrice, int quantity, String condition, String... attributes) {
        // Mặc định nếu không truyền attributes
        String artist = "Unknown Artist";
        int year = 0;

        // Bóc tách attributes: attributes[0] là artist, attributes[1] là year
        if (attributes != null && attributes.length > 0) {
            artist = attributes[0];
        }

        if (attributes != null && attributes.length > 1) {
            try {
                year = Integer.parseInt(attributes[1]);
            } catch (NumberFormatException e) {
                System.err.println("Lỗi định dạng năm cho tác phẩm nghệ thuật: " + attributes[1]);
                year = 0; // Hoặc gán năm hiện tại tùy logic của bạn
            }
        }

        // Tạo và trả về đối tượng Art
        return new Art(name, description, startingPrice, quantity, condition, artist, year, sellerId);
    }
}