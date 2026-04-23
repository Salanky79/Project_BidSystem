package com.auction.share.models.item;

import com.auction.share.models.core.Entity;
import com.auction.share.enums.Category;

public class Item extends Entity {
    private String name;
    private String description = "";
    private double startingPrice;
    private int quantity;
    private String condition;
    private String sellerId;
    private Category category;

    public Item(String name, String description, double startingPrice, int quantity,
                String condition, String sellerId) {
        super();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.quantity = quantity;
        this.condition = condition;
        this.sellerId = sellerId;
    }

    public void setCategory(Category category){
        this.category = category;
    }
    public Category getCategory(){
        return category;
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
    public int getQuantity() {
        return quantity;
    }
    public String getCondition() {
        return condition;
    }
    public String getSellerId() {
        return sellerId;
    }
}

