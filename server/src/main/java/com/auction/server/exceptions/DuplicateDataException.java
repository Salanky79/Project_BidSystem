package com.auction.server.exceptions;

// Quăng khi dữ liệu đã tồn tại trong database
public class DuplicateDataException extends RuntimeException {
    public DuplicateDataException(String message) {
        super(message);
    }

    public DuplicateDataException(String message, Throwable cause) {
        super(message, cause);
    }
}