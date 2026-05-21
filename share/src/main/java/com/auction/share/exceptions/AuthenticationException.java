package com.auction.share.exceptions;

/**
 * Xác thực không hợp lệ hoặc chưa đăng nhập.
 */
public class AuthenticationException extends AuctionSystemException {
    public AuthenticationException(String message) {
        super(message);
    }
}