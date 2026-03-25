public class Bidder extends User {
    private double balance;
    private String address;
    private BidTransaction[] bidHistory;

    public void Auction(String Item_id, double amount) {
        //Nhap id cua vat pham
        //Nhap gia
    }
    public void Deposit(double amount) {
        this.balance += amount;
        //Nap tien
    }
}