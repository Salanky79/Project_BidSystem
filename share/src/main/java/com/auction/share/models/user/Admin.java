package com.auction.share.models.user;

import com.auction.share.enums.Role;

/**
 * Đại diện cho Quản trị viên (Admin) trong hệ thống đấu giá.
 * Có quyền quản lý người dùng, sản phẩm và các phiên đấu giá tùy thuộc vào mức độ truy cập.
 */
public class Admin extends User {
    /**
     * Cấp độ truy cập của quản trị viên (ví dụ: 1 là Super Admin, 2 là Moderator, v.v.).
     */
    private int accessLevel;

    /**
     * Khởi tạo đối tượng Quản trị viên.
     * Mặc định gán vai trò là Role.ADMIN.
     *
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @param fullName Tên đầy đủ
     * @param accessLevel Cấp độ truy cập của Admin
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