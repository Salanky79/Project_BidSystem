package com.auction.share.models.item;

import com.auction.share.enums.Category;
import com.auction.share.models.core.Entity;

/**
 * Đại diện cho một Sản phẩm (Item) tham gia đấu giá.
 * Kế thừa từ thực thể cơ sở (Entity), dùng làm cha cho các sản phẩm chuyên biệt như Xe cộ, Bất động sản...
 */
public class Item extends Entity {
    /**
     * Tên sản phẩm.
     */
    private String name;

    /**
     * Mô tả chi tiết về tình trạng, đặc điểm của sản phẩm.
     */
    private String description;

    /**
     * Giá khởi điểm của sản phẩm do người bán thiết lập.
     */
    private double startingPrice;

    /**
     * ID của người bán (Seller) đăng bán sản phẩm này.
     */
    private String sellerId;

    /**
     * Danh mục phân loại sản phẩm.
     */
    private Category category;

    /**
     * Khởi tạo một sản phẩm với phân loại mặc định là Category.ITEM.
     *
     * @param name          Tên sản phẩm
     * @param description   Mô tả chi tiết
     * @param startingPrice Giá khởi điểm
     * @param sellerId      Người bán sản phẩm
     */
    public Item(String name, String description, double startingPrice, String sellerId) {
        this(name, description, startingPrice, sellerId, Category.ITEM);
    }

    /**
     * Khởi tạo một sản phẩm với các thuộc tính tuỳ chỉnh.
     *
     * @param name          Tên sản phẩm
     * @param description   Mô tả chi tiết
     * @param startingPrice Giá khởi điểm
     * @param sellerId      Người bán sản phẩm
     * @param category      Danh mục của sản phẩm
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