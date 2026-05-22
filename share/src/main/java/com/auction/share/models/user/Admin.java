package com.auction.share.models.user;

import com.auction.share.enums.Role;

/**
 * Quản trị viên và cấp độ truy cập.
 */
public class Admin extends User {
    private int accessLevel;

    /**
     * Tạo admin với cấp độ truy cập.
     */
    public Admin(String username, String password, String fullName, int accessLevel) {
        super(username, password, fullName);
        this.accessLevel = accessLevel;
        this.setRole(Role.ADMIN);
    }

    public int getAccessLevel(){
        return accessLevel;
    }
}