package src.models.user;

import Enum.Role;


public class Admin extends User {
    private int accessLevel; // Cấp độ quản trị (VD: 1 - Mod, 2 - Super Admin)

    public Admin(String username, String password, String fullName, String uid, int accessLevel) {
        super(username, password, fullName, uid);
        this.accessLevel = accessLevel;
        this.setRole(Role.ADMIN);
    }

    // Các hàm đặc quyền của Admin
    public void banUser(User user) {
        System.out.println("Admin đã khóa tài khoản: " + user.getUsername());
    }
}