package com.auction.server.controllers;

import com.auction.share.models.item.Item;

public interface ItemCreator {
    Item createItem(String name, String description, double startingPrice, int quantity, String condition, String... attributes);
}