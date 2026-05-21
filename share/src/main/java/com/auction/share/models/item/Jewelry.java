package com.auction.share.models.item;

import com.auction.share.enums.Category;

/**
 * Đại diện cho một sản phẩm đấu giá thuộc danh mục Trang sức (Jewelry).
 * Đặc trưng bởi chất liệu và trọng lượng (đơn vị carat).
 */
public class Jewelry extends Item {
    /**
     * Chất liệu của trang sức (VD: Vàng 24k, Bạch kim, Bạc).
     */
    private String material;

    /**
     * Trọng lượng của trang sức (thường tính bằng Carat cho kim cương/đá quý).
     */
    private double caratWeight;

    /**
     * Khởi tạo một sản phẩm trang sức.
     * Mặc định danh mục sẽ được gán là Category.JEWELRY.
     *
     * @param name          Tên món trang sức
     * @param description   Mô tả chi tiết chứng nhận, độ tinh khiết
     * @param startingPrice Giá khởi điểm
     * @param sellerID      Mã người bán
     * @param material      Chất liệu cấu thành
     * @param caratWeight   Trọng lượng Carat
     */
    public Jewelry(String name, String description, double startingPrice,  String sellerID, String material, double caratWeight) {
        super(name, description, startingPrice, sellerID, Category.JEWELRY);
        this.material = material;
        this.caratWeight = caratWeight;
    }

    public String getMaterial() {
        return material;
    }
    public double getCaratWeight() {
        return caratWeight;
    }
}