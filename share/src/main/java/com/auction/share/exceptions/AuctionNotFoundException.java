package com.auction.share.exceptions;

/**
 * Ném ra khi không tìm thấy phiên đấu giá tương ứng với ID yêu cầu.
 */
public class AuctionNotFoundException extends AuctionSystemException {
    /**
     * Tạo mới exception với thông báo lỗi tùy chỉnh.
     *
     * @param message Thông báo chứa mã ID không tồn tại.
     */
    public AuctionNotFoundException(String message) {
        super(message);
    }
}