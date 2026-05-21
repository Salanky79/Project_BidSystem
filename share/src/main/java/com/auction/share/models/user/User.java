package com.auction.share.models.user;

import com.auction.share.enums.Role;
import com.auction.share.models.core.Entity;

/**
 * Lớp trừu tượng đại diện cho một người dùng trong hệ thống đấu giá.
 * Bao gồm các thông tin cơ bản như tên đăng nhập, mật khẩu, tên đầy đủ và vai trò.
 */
public abstract class User extends Entity{
    /**
     * Tên đăng nhập (duy nhất) của người dùng dùng để đăng nhập vào hệ thống.
     */
    private String username;

    /**
     * Mật khẩu (đã được mã hóa) của người dùng.
     */
    private String password;

    /**
     * Tên hiển thị đầy đủ của người dùng.
     */
    private String fullName;

    /**
     * Vai trò của người dùng (BIDDER, SELLER, ADMIN).
     */
    private Role role;

    /**
     * Khởi tạo một đối tượng người dùng mới.
     *
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @param fullName Tên đầy đủ
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