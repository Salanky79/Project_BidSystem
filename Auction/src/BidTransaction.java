import java.time.LocalDateTime;

public class BidTransaction extends Entity {
    // Thêm chữ FINAL vào tất cả các biến
    private final Auction auction;
    private final Bidder bidder;
    private final double amount;
    private final LocalDateTime timestamp;

    public BidTransaction(Auction auction, Bidder bidder, double amount) {
        super();
        this.auction = auction;
        this.bidder = bidder;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    // Chỉ có hàm GET (Đọc), tuyệt đối KHÔNG viết hàm SET (Sửa)
    public Bidder getBidder() { return bidder; }
    public double getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public String getTransactionDetails() {
        return "Tài khoản [" + bidder.getUsername() + "] đã đặt " +
                amount + " VNĐ vào lúc " + timestamp.toString();
    }
}