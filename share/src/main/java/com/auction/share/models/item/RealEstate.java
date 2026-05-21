package com.auction.share.models.item;

import com.auction.share.enums.Category;

/**
 * Đại diện cho một sản phẩm đấu giá thuộc danh mục Bất động sản (RealEstate).
 * Đặc trưng bởi vị trí địa lý và diện tích.
 */
public class RealEstate extends Item {
    /**
     * Vị trí hoặc địa chỉ chi tiết của bất động sản.
     */
    private String location;

    /**
     * Diện tích của bất động sản (tính bằng mét vuông).
     */
    private double areaSquareMeter;

    /**
     * Khởi tạo một sản phẩm bất động sản.
     * Mặc định danh mục sẽ được gán là Category.REALESTATE.
     *
     * @param name            Tên bất động sản (VD: Biệt thự ven biển)
     * @param description     Mô tả chi tiết về giấy tờ pháp lý, tiện ích
     * @param startingPrice   Giá khởi điểm
     * @param sellerId        Mã người bán
     * @param location        Vị trí địa lý
     * @param areaSquareMeter Diện tích tính bằng mét vuông
     */
    public RealEstate(String name, String description, double startingPrice, String sellerId,
                      String location, double areaSquareMeter) {
        super(name, description, startingPrice, sellerId, Category.REALESTATE);
        this.location = location;
        this.areaSquareMeter = areaSquareMeter;
    }

    public String getLocation() {
        return location;
    }
    public double getAreaSquareMeter() {
        return areaSquareMeter;
    }
}