package com.auction.server.controllers;


import com.auction.share.models.item.Item;

import java.util.HashMap;
import java.util.Map;

public abstract class ItemFactory {
    private static final Map<String, ItemCreator> creators = new HashMap<>();

    static {
        creators.put("art", new ArtFactory());
        creators.put("electronic", new ElectronicFactory());
        creators.put("jewelry", new JewelryFactory());
        creators.put("realestate", new RealEstateFactory());
        creators.put("vehicle", new VehicleFactory());
    }

    public static Item createItem(String type, String name, String description, double startingPrice, int quantity,
                                  String condition, String... attributes) {


        ItemCreator creator = creators.get(type.toLowerCase());
        if (creator == null) {
            throw new IllegalArgumentException("Loại hàng '" + type + "' không được hỗ trợ!");
        }
        return creator.createItem(name, description, startingPrice, quantity, condition, attributes);
    }
}
