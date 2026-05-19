package com.auction.share.models.item;

import com.auction.share.enums.Category;

/**
 * Đại diện cho một sản phẩm đấu giá thuộc danh mục Xe cộ (Vehicle).
 * Đặc trưng bởi loại nhiên liệu sử dụng.
 */
public class Vehicle extends Item {
    /**
     * Loại nhiên liệu mà xe sử dụng (VD: Xăng, Dầu, Điện).
     */
    private String fuelType;

    /**
     * Khởi tạo một sản phẩm xe cộ.
     * Mặc định danh mục sẽ được gán là Category.VEHICLE.
     *
     * @param name          Tên dòng xe
     * @param description   Mô tả chi tiết về máy móc, đời xe
     * @param startingPrice Giá khởi điểm
     * @param sellerId      Mã người bán
     * @param fuelType      Loại nhiên liệu sử dụng
     */
    public Vehicle(String name, String description, double startingPrice, String sellerId,
                   String fuelType) {
        super(name, description, startingPrice, sellerId, Category.VEHICLE);
        this.fuelType = fuelType;
    }

    public String getFuelType(){
        return fuelType;
    }
}