package com.auction.share.models.item;

import com.auction.share.enums.Category;

/**
 * Đại diện cho một sản phẩm đấu giá thuộc danh mục Nghệ thuật (Art).
 * Các thuộc tính đặc trưng bao gồm tên họa sĩ/nghệ sĩ và năm sáng tác.
 */
public class Art extends Item {
    /**
     * Tên nghệ sĩ hoặc họa sĩ sáng tác tác phẩm.
     */
    private String artist;

    /**
     * Năm tác phẩm được sáng tác.
     */
    private int year;

    /**
     * Khởi tạo một tác phẩm nghệ thuật.
     * Mặc định danh mục sẽ được gán là Category.ART.
     *
     * @param name          Tên tác phẩm nghệ thuật
     * @param description   Mô tả chi tiết tác phẩm (chất liệu, kích thước...)
     * @param startingPrice Giá khởi điểm của tác phẩm
     * @param sellerId      Mã người bán
     * @param artist        Tên nghệ sĩ sáng tác
     * @param year          Năm sáng tác
     */
    public Art(String name, String description, double startingPrice,String sellerId, String artist, int year ) {
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