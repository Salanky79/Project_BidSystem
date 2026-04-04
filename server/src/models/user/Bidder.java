public class Bidder extends User {
    private double balance; // Số dư tài khoản

    public Bidder(String username, String password, double balance) {
        super(username, password);
        this.balance = balance;
        this.setRole(Role.BIDDER);
    }

    public double getBalance() {
        return balance;
    }
}