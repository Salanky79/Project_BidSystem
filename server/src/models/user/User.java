public abstract class User extends Entity {
    private String username;
    private String password;
    private Role role;

    public User(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public void setRole(Role role){
        this.role = role;
    }
    public String getUsername() {
        return username;
    }

}