package com.auction.share.models.user;

import com.auction.share.enums.Role;

public class Admin extends User {
    private int accessLevel;

    public Admin(String username, String password, String fullName, String uid, int accessLevel) {
        super(username, password, fullName, uid);
        this.accessLevel = accessLevel;
        this.setRole(Role.ADMIN);
    }

    public void banUser(User user) {
        System.out.println("Admin đã khóa tài khoản: " + user.getUsername());
    }
}

