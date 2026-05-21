package com.auction.share.enums;

/**
 * Thể hiện các trạng thái của một phiên đấu giá trong hệ thống.
 */
public enum AuctionStatus{
    /**
     * Phiên đấu giá đang mở, chờ người tham gia hoặc chờ đủ điều kiện bắt đầu.
     */
    OPEN,

    /**
     * Phiên đấu giá đang diễn ra, người dùng có thể đặt giá.
     */
    RUNNING,

    /**
     * Phiên đấu giá đã kết thúc thành công (đã có người thắng cuộc hoặc hết thời gian).
     */
    FINISHED,

    /**
     * Phiên đấu giá bị hủy do quản trị viên hoặc lỗi hệ thống.
     */
    CANCELED
}
