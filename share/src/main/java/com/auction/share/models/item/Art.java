package com.auction.share.models.item;

import com.auction.share.enums.Category;

public class Art extends Item {
    private String artist;
    private int year;

    public Art(String name, String description, double startingPrice, int quantity,
               String condition, String artist, int year, String sellerId) {
        super(name, description, startingPrice, quantity, condition, sellerId);
        this.artist = artist;
        this.year = year;
        this.setCategory(Category.ART);
    }

    public String getArtist() {
        return artist;
    }
    public int getYear() {
        return year;
    }
}

