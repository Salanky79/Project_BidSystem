package com.auction.share.models.item;

import com.auction.share.enums.Category;

/**
 * Sản phẩm danh mục nghệ thuật.
 */
public class Art extends Item {
    private String artist;
    private int year;

    public Art(String name, String description, double startingPrice, String sellerId, String artist, int year) {
        super(name, description, startingPrice, sellerId, Category.ART);
        this.artist = artist;
        this.year = year;
    }

    public String getArtist() {
        return artist;
    }
    public int getYear() {
        return year;
    }
}