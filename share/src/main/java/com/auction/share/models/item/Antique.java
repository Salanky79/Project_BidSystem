package com.auction.share.models.item;

import com.auction.share.enums.Category;

/**
 * Đại diện cho một sản phẩm đấu giá thuộc danh mục Đồ cổ (Antique).
 * Các thuộc tính đặc trưng bao gồm thời đại/niên đại và chất liệu.
 */
public class Antique extends Item {
    /**
     * Thời đại hoặc niên đại của món đồ cổ (VD: Nhà Nguyễn, Thế kỷ XVIII).
     */
    private String era;

    /**
     * Chất liệu cấu thành nên món đồ cổ (VD: Gốm sứ, Đồng, Gỗ mun).
     */
    private String material;

    /**
     * Khởi tạo một sản phẩm đồ cổ.
     * Mặc định danh mục sẽ được gán là Category.ANTIQUE.
     *
     * @param name          Tên món đồ cổ
     * @param description   Mô tả chi tiết tình trạng, xuất xứ
     * @param startingPrice Giá khởi điểm
     * @param sellerId      Mã người bán
     * @param era           Niên đại/Thời đại
     * @param material      Chất liệu
     */
    public Antique(String name, String description, double startingPrice, String sellerId, String era, String material) {
        super(name, description, startingPrice, sellerId, Category.ANTIQUE);
        this.era = era;
        this.material = material;
    }

    public String getEra() {
        return era;
    }
    public String getMaterial() {
        return material;
    }
}