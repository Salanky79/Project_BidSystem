package src.models.user;

import Enum.Role;

public class Admin{
    public Admin(String username, String password){
        this.username = username;
        this.password = password;
        this.setRole(Role.ADMIN);
    }
}