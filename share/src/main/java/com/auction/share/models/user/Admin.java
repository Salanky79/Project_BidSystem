package com.auction.share.models.user;

import com.auction.share.enums.Role;

public class Admin extends User {
    private int accessLevel;

    public Admin(String username, String password, String fullName, int accessLevel) {
        super(username, password, fullName);
        this.accessLevel = accessLevel;
        this.setRole(Role.ADMIN);
    }

    public int getAccessLevel(){
        return accessLevel;
    }
    public void banUser(User user) {
        System.out.println("Admin đã khóa tài khoản: " + user.getUsername());
    }
}

