package com.auction.share.enums;

/**
 * Thể hiện vai trò của người dùng trong hệ thống đấu giá.
 * Quyết định quyền hạn và các hành động mà người dùng có thể thực hiện.
 */
public enum Role{
    /**
     * Người tham gia trả giá. Có thể nạp tiền và đấu giá sản phẩm.
     */
    BIDDER,

    /**
     * Người đăng bán sản phẩm. Có quyền tạo phiên đấu giá mới.
     */
    SELLER,

    /**
     * Quản trị viên hệ thống. Có toàn quyền quản lý người dùng và phiên đấu giá.
     */
    ADMIN,
}
