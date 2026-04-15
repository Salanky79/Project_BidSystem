package com.auction.server.controllers;

import com.auction.share.models.item.Item;
import com.auction.share.enums.Category;

public class ItemFactory {

    /**
     * Tạo một món hàng mặc định cho đấu giá.
     * Giúp giảm bớt việc phải truyền description hay quantity thủ công.
     */
    public static Item createDefaultItem(String name, double startingPrice, String condition) {
        // Factory tự điền description mặc định và quantity = 1
        return new Item(
                name,
                "Mô tả mặc định cho " + name,
                startingPrice,
                1,
                condition
        );
    }

    /**
     * Tạo một món hàng kèm theo danh mục (Category)
     */
    public static Item createItemWithCategory(String name, double price, String condition, Category category) {
        Item item = new Item(name, "Hàng thuộc danh mục " + category, price, 1, condition);
        item.setCategory(category); // Factory xử lý luôn việc set category
        return item;
    }

    /**
     * Tạo món hàng cao cấp (Premium)
     */
    public static Item createPremiumItem(String name, double price) {
        // Hàng Premium mặc định là mới (New) và có mô tả xịn
        return new Item(name, "Sản phẩm cao cấp được kiểm định", price, 1, "New");
    }
}