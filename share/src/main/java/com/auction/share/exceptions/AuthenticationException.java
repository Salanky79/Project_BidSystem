package com.auction.share.exceptions;

/**
 * Ném ra khi thông tin đăng nhập (như username hoặc password) không chính xác,
 * hoặc người dùng chưa đăng nhập nhưng cố gắng truy cập tính năng cần xác thực.
 */
public class AuthenticationException extends AuctionSystemException {
    /**
     * Khởi tạo ngoại lệ do lỗi xác thực.
     *
     * @param message Thông điệp mô tả chi tiết lỗi (vd: Mật khẩu không đúng).
     */
    public AuthenticationException(String message) {
        super(message);
    }
}