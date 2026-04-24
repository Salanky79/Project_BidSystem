package com.auction.share.exceptions;

// Quăng khi giá đặt thấp hơn giá hiện tại
public class BidTooLowException extends RuntimeException {
    public BidTooLowException(String message) {
        super(message);
    }

    public BidTooLowException(String message, Throwable cause) {
        super(message, cause);
    }
}