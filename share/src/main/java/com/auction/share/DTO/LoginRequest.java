package com.auction.share.DTO;

/**
 * Yêu cầu đăng nhập vào hệ thống.
 */
public class LoginRequest extends Request {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;

    public LoginRequest(String username, String password) {
        super(Action.LOGIN);
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
