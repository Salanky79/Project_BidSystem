package src.models.item;

import src.models.core.Entity;

public class Item extends Entity {
    private String name;
    private String description;
    private double startingPrice;
    private int quantity;
    private String condition;
    private long sellerId;

    public Item(String name, String description, double startingPrice, int quantity,
                String condition) {
        super();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.quantity = quantity;
        this.condition = condition;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getStartingPrice() { return startingPrice; }
    public int getQuantity() { return quantity; }
    public String getCondition() { return condition; }
    public long getSellerId() { return sellerId; }
}