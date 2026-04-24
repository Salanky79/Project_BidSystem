package com.auction.share.exceptions;

// Quăng khi giá đặt không hợp lệ (âm, 0, hoặc không phải số)
public class InvalidBidException extends RuntimeException {
    public InvalidBidException(String message) {
        super(message);
    }

    public InvalidBidException(String message, Throwable cause) {
        super(message, cause);
    }
}