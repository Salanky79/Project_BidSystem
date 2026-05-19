package com.auction.share.models.auction;

import com.auction.share.models.core.Entity;
import com.auction.share.models.user.Bidder;
import java.time.LocalDateTime;

/**
 * Đại diện cho một giao dịch trả giá (Lịch sử trả giá) trong hệ thống.
 * Chứa thông tin về việc một tài khoản (Bidder) đã đưa ra mức giá nào, cho phiên đấu giá nào và vào thời điểm cụ thể nào.
 * Giao dịch này là bất biến (immutable) với các thuộc tính cơ bản để đảm bảo tính minh bạch.
 */
public class BidTransaction extends Entity {
    /**
     * Phiên đấu giá tương ứng với giao dịch này.
     */
    private final Auction auction;

    /**
     * Người trả giá thực hiện giao dịch này.
     */
    private final Bidder bidder;

    /**
     * Số tiền (mức giá) mà bidder đã đưa ra.
     */
    private final double amount;

    /**
     * Dấu thời gian ghi nhận lúc giao dịch trả giá thành công.
     */
    private LocalDateTime timestamp;

    /**
     * Khởi tạo một giao dịch trả giá mới.
     * Mặc định ghi nhận thời gian tại thời điểm tạo giao dịch.
     *
     * @param auction Phiên đấu giá liên quan
     * @param bidder  Người thực hiện trả giá
     * @param amount  Số tiền trả giá
     */
    public BidTransaction(Auction auction, Bidder bidder, double amount) {
        super();
        this.auction = auction;
        this.bidder = bidder;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    public Auction getAuction() { return auction; }
    public Bidder getBidder() { return bidder; }
    public double getAmount() { return amount; }
    public void setTimestamp(LocalDateTime timestamp){
        this.timestamp = timestamp;
    }
    public LocalDateTime getTimestamp() { return timestamp; }

    /**
     * Trả về thông tin chi tiết của giao dịch dưới dạng chuỗi văn bản.
     * Thường dùng để in ra log hoặc hiển thị trên giao diện theo dõi.
     *
     * @return Chuỗi mô tả thông tin giao dịch
     */
    public String getTransactionDetails() {
        return "Account [" + bidder.getUsername() + "] placed " +
                amount + " VND at " + timestamp;
    }
}