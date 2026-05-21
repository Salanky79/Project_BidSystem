package com.auction.server.factory;

import com.auction.share.models.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory tạo Item theo category.
 */
public final class ItemFactory {
    private static final Map<String, ItemCreator> CREATORS = new HashMap<>();

    static {
        register("ART", new ArtFactory());
        register("ELECTRONIC", new ElectronicFactory());
        register("JEWELRY", new JewelryFactory());
        register("REALESTATE", new RealEstateFactory());
        register("VEHICLE", new VehicleFactory());
        register("ANTIQUE", new AntiqueFactory());
    }

    private ItemFactory() {
    }

    public static void register(String category, ItemCreator creator) {
        CREATORS.put(normalizeCategory(category), creator);
    }


    public static Item createItem(
            String category,
            String name,
            String description,
            double startingPrice,
            String sellerId,
            String... attributes
    ) {
        ItemCreator creator = CREATORS.get(normalizeCategory(category));
        if (creator == null) {
            throw new IllegalArgumentException("Unsupported item category: " + category);
        }
        return creator.createItem(name, description, startingPrice, sellerId, attributes);
    }

    private static String normalizeCategory(String category) {
        return category.trim().toUpperCase();
    }
}
