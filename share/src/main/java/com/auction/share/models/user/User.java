package com.auction.share.models.user;

import com.auction.share.enums.Role;
import com.auction.share.models.core.Entity;

public abstract class User extends Entity{
    private String username;
    private String password;
    private String fullName;
    private Role role;

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
    public double getBalance() {
        return 0;
    }
    public String getAddress() {
        return null;
    }
    public int getAccessLevel() {
        return 0;
    }
}

