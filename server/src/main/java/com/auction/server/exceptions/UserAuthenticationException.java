package com.auction.server.exceptions;

// Quăng lỗi khi login thất bại (username/password sai)
public class UserAuthenticationException extends RuntimeException {
    public UserAuthenticationException(String message) {
        super(message);
    }

    public UserAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}