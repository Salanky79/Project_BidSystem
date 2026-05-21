package com.auction.server.factory;

import com.auction.share.models.item.Item;

/**
 * Interface tạo Item theo dữ liệu đầu vào.
 */
public interface ItemCreator {
    Item createItem(String name, String description, double startingPrice, String sellerID, String... attributes);
}