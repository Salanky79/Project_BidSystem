package com.auction.client.utils;

public final class CategoryUtils {
    private CategoryUtils() {}

    public static String iconFor(String category) {
        if (category == null) return "📦";
        return switch (category.trim()) {
            case "Antique"              -> "🏺";
            case "Art"                  -> "🖼";
            case "Electronic"           -> "📱";
            case "Jewelry"              -> "💍";
            case "RealEstate"           -> "🏠";
            case "Vehicle", "Car"       -> "🚗";
            case "Watch", "WATCH"       -> "⌚";
            case "Hand Bag", "Clothing" -> "👜";
            default                     -> "📦";
        };
    }
}
