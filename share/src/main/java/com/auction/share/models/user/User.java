package com.auction.share.models.user;

import com.auction.share.enums.Role;
import com.auction.share.models.core.Entity;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

    public void setPassword(String password){
        this.password = password;
    }

    // Trong abstract class User
    public abstract void fillPreparedStatement(PreparedStatement ps) throws SQLException;
}

