package com.auction.share.enums;

/**
 * Danh mục các loại sản phẩm được phép đấu giá trên hệ thống.
 * Hỗ trợ việc phân loại, tìm kiếm và hiển thị thuộc tính đặc thù.
 */
public enum Category {
    /** Sản phẩm thông thường (mặc định) */
    ITEM,

    /** Đồ cổ, có giá trị lịch sử và thời gian */
    ANTIQUE,

    /** Tác phẩm nghệ thuật như tranh, tượng... */
    ART,

    /** Thiết bị điện tử, công nghệ */
    ELECTRONIC,

    /** Đồ trang sức, đá quý */
    JEWELRY,

    /** Bất động sản, nhà đất */
    REALESTATE,

    /** Xe cộ, phương tiện đi lại */
    VEHICLE
}
