package com.auction.share.exceptions;

/**
 * Ném ra khi có lỗi liên quan đến đăng nhập hoặc phân quyền.
 */
public class AuthenticationException extends AuctionSystemException {
    public AuthenticationException(String message) {
        super(message);
    }
}