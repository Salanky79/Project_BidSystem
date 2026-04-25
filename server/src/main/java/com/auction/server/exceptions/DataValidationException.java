package com.auction.server.exceptions;

// quăng lỗi khi data không hợp lệ. VD: username null, email sai format...
public class DataValidationException extends RuntimeException {
    public DataValidationException(String message) {
        super(message);
    }

    public DataValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}