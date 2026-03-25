public class abstract class User extends Entity {
    private String username;
    private String password;
    private String fullName;

    public abstract boolean login();
    public abstract void logout();
}