package src.models.user;

import Enum.Role;
import src.models.core.Entity;

public abstract class User extends Entity {
    private String username;
    private String password;
    private String fullName;
    private String uid; //user's Id
    private Role role;

    //Constructor
    public User(String username, String password, String fullName, String uid) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.uid = uid;
    }

    public void setRole(Role role){
        this.role = role;
    }

    public String getFullName(){
        return this.fullName;
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){ return  this.password;}
}