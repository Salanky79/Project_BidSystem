public class Seller extends User {
    private String storeName;

    public Seller(String username, String password, String storeName) {
        super(username, password);
        this.storeName = storeName;
    }
}