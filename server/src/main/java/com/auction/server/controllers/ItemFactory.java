package com.auction.server.controllers;


import com.auction.share.models.item.Item;
import com.auction.share.enums.Category;

public class ItemFactory {
    // Factory Method: Tập trung việc tạo đối tượng vào một chỗ

    //Phương thức factory cơ bản - tạo item với mô tả mặc định
    public static Item createItem(String name, double startingPrice, String condition) {
        return new Item(name, "Hang dau gia", startingPrice, 1, condition);
    }

    //Factory method tạo item với mô tả tùy chỉnh
    public static Item createItem(String name, String description, double startingPrice,
                                   int quantity, String condition) {
        return new Item(name, description, startingPrice, quantity, condition);
    }

    //Factory method tạo item điện tử (Electronics)
    public static Item createElectronicsItem(String name, String description,
                                             double startingPrice, String condition) {
        Item item = new Item(name, description, startingPrice, 1, condition);
        item.setCategory(Category.ELECTRONICS);
        return item;
    }

    //Factory method tạo item nghệ thuật (Art)
    public static Item createArtItem(String name, String description,
                                      double startingPrice, String condition) {
        Item item = new Item(name, description, startingPrice, 1, condition);
        item.setCategory(Category.ART);
        return item;
    }

    // Factory method tạo item trang sức (Jewelry)
    public static Item createJewelryItem(String name, String description,
                                         double startingPrice, String condition) {
        Item item = new Item(name, description, startingPrice, 1, condition);
        item.setCategory(Category.JEWELRY);
        return item;
    }

    // Factory method tạo item bất động sản (Real Estate)
    public static Item createRealEstateItem(String name, String description,
                                            double startingPrice, int quantity, String condition) {
        Item item = new Item(name, description, startingPrice, quantity, condition);
        item.setCategory(Category.REAL_ESTATE);
        return item;
    }

    // Factory method tạo item phương tiện (Vehicle)
    public static Item createVehicleItem(String name, String description,
                                         double startingPrice, String condition) {
        Item item = new Item(name, description, startingPrice, 1, condition);
        item.setCategory(Category.VEHICLE);
        return item;
    }

    // Factory method tạo item cổ vật (Antique)
    public static Item createAntiqueItem(String name, String description,
                                         double startingPrice, String condition) {
        Item item = new Item(name, description, startingPrice, 1, condition);
        item.setCategory(Category.ANTIQUE);
        return item;
    }

    // Factory method tạo item với category tùy chỉnh
    public static Item createItem(String name, String description, double startingPrice,
                                  int quantity, String condition, Category category) {
        Item item = new Item(name, description, startingPrice, quantity, condition);
        item.setCategory(category);
        return item;
    }
}