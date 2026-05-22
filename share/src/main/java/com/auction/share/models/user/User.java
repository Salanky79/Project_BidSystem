package com.auction.share.models.user;

import com.auction.share.enums.Role;
import com.auction.share.models.core.Entity;

/**
 * Thông tin người dùng và vai trò hệ thống.
 */
public abstract class User extends Entity {
    private String username;
    private String password;
    private String fullName;
    private Role role;

    /**
     * Tạo người dùng với thông tin cơ bản.
     */
    public User(String username, String password, String fullName) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    public void setRole(Role role){
        this.role = role;
    }
    public Role getRole(){
        return role;
    }
    public String getFullName(){
        return this.fullName;
    }
    public String getUsername(){
        return this.username;
    }
    public String getPassword(){
        return  this.password;
    }
    public void setPassword(String password){
        this.password = password;
    }
}