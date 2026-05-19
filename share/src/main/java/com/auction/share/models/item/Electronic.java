package com.auction.share.models.item;

import com.auction.share.enums.Category;

/**
 * Đại diện cho một sản phẩm đấu giá thuộc danh mục Thiết bị điện tử (Electronic).
 * Đặc trưng bởi thương hiệu và thời gian bảo hành.
 */
public class Electronic extends Item {
    /**
     * Thương hiệu của thiết bị điện tử (VD: Apple, Samsung, Sony).
     */
    private String brand;

    /**
     * Thời gian bảo hành còn lại, tính bằng tháng.
     */
    private int warrantyMonths;

    /**
     * Khởi tạo một sản phẩm thiết bị điện tử.
     * Mặc định danh mục sẽ được gán là Category.ELECTRONIC.
     *
     * @param name           Tên thiết bị điện tử
     * @param description    Mô tả chi tiết cấu hình, tình trạng
     * @param startingPrice  Giá khởi điểm
     * @param sellerId       Mã người bán
     * @param brand          Thương hiệu sản xuất
     * @param warrantyMonths Số tháng bảo hành còn lại
     */
    public Electronic(String name, String description, double startingPrice, String sellerId, String brand, int warrantyMonths) {
        super(name, description, startingPrice, sellerId, Category.ELECTRONIC);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    public String getBrand() {
        return brand;
    }
    public int getWarrantyMonths() {
        return warrantyMonths;
    }
}