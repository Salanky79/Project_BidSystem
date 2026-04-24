package com.auction.share.exceptions;

// Quăng khi một người không có đủ tiền để thực hiện giao dịch
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }
}