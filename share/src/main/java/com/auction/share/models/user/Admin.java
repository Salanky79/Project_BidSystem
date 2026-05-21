package com.auction.share.models.user;

import com.auction.share.enums.Role;

/**
 * Quản trị viên hệ thống.
 */
public class Admin extends User {
    private int accessLevel;

    public Admin(String username, String password, String fullName, int accessLevel) {
        super(username, password, fullName);
        this.accessLevel = accessLevel;
        this.setRole(Role.ADMIN);
    }

    public int getAccessLevel() {
        return accessLevel;
    }
}