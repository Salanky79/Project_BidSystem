package com.auction.server.controllers;


import com.auction.share.models.item.Item;

public class ItemFactory {
    // Factory Method: Tập trung việc tạo đối tượng vào một chỗ
    public static Item createItem(String name, double startingPrice, String sellerId) {
        // Bạn có thể thêm logic mặc định ở đây (Ví dụ: mô tả mặc định)
        return new Item(name, "Hang dau gia", startingPrice, sellerId);
    }
}