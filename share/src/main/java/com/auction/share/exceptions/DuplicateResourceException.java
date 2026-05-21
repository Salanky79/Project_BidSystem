package com.auction.share.exceptions;

/**
 * Ném ra khi người dùng cố gắng tạo mới một bản ghi đã tồn tại trong hệ thống,
 * ví dụ như việc đăng kí tài khoản với một email đã có người sử dụng.
 */
public class DuplicateResourceException extends AuctionSystemException {
    /**
     * Lỗi trùng lặp dữ liệu.
     *
     * @param message Thông báo mô tả rõ dữ liệu nào đã bị trùng lặp.
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
}
