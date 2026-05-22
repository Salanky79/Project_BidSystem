package com.auction.share.exceptions;

/**
 * Lỗi xác thực đăng nhập hoặc quyền truy cập.
 */
public class AuthenticationException extends AuctionSystemException {
    public AuthenticationException(String message) {
        super(message);
    }
}