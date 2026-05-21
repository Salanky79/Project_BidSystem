package com.auction.share.models.item;

import com.auction.share.enums.Category;
import com.auction.share.models.core.Entity;

/**
 * Sản phẩm tham gia đấu giá.
 */
public class Item extends Entity {
    private String name;
    private String description;
    private double startingPrice;
    private String sellerId;
    private Category category;

    /**
     * Tạo sản phẩm với danh mục mặc định ITEM.
     */
    public Item(String name, String description, double startingPrice, String sellerId) {
        this(name, description, startingPrice, sellerId, Category.ITEM);
    }

    /**
     * Tạo sản phẩm với danh mục chỉ định.
     */
    public Item(String name, String description, double startingPrice, String sellerId, Category category) {
        super();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.sellerId = sellerId;
        this.category = category;
    }

    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public double getStartingPrice() {
        return startingPrice;
    }
    public String getSellerId() {
        return sellerId;
    }
    public Category getCategory() {
        return category;
    }
}