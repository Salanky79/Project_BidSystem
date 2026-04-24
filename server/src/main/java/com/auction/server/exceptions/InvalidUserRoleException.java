package com.auction.server.exceptions;

// Quăng khi người dùng không có quyền để thực hiện hành động
public class InvalidUserRoleException extends RuntimeException {
    public InvalidUserRoleException(String message) {
        super(message);
    }

    public InvalidUserRoleException(String message, Throwable cause) {
        super(message, cause);
    }
}