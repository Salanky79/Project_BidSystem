import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Auction extends Entity {
    private Item item;
    private Seller seller;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private double currentHighestBid;
    private Bidder highestBidder;
    private AuctionStatus status;
    private List<BidTransaction> bidHistory;

    // Constructor: Khởi tạo phiên đấu giá
    public Auction(Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime) {
        super();
        this.item = item;
        this.seller = seller;
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentHighestBid = item.getStartingPrice(); // Giá khởi điểm lấy từ Item
        this.highestBidder = null;
        this.status = AuctionStatus.OPEN; // Trạng thái mặc định là Chờ mở cửa
        this.bidHistory = new ArrayList<>();
    }

    // Mở phiên đấu giá
    public void startAuction() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            System.out.println("Chưa tới giờ bắt đầu phiên đấu giá!");
            return;
        }
        this.status = AuctionStatus.RUNNING;
        System.out.println("\n[HỆ THỐNG] Phiên đấu giá cho [" + item.getName() + "] BẮT ĐẦU!");
    }


    // CHỨC NĂNG (mục 3.1.3): THAM GIA ĐẤU GIÁ


    public synchronized boolean processBid(Bidder bidder, double amount) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Kiểm tra thời gian (Time Bound)
        if (now.isBefore(startTime)) {
            throw new RuntimeException("Lỗi: Phiên đấu giá chưa tới giờ bắt đầu!");
        }
        if (now.isAfter(endTime)) {
            // Tự động đóng phiên nếu phát hiện đã quá giờ
            closeAuction();
            throw new RuntimeException("Lỗi: Phiên đấu giá đã kết thúc vào lúc " + endTime);
        }

        // 2. Kiểm tra trạng thái
        if (this.status != AuctionStatus.RUNNING) {
            throw new RuntimeException("Lỗi: Phiên đấu giá không ở trạng thái hoạt động!");
        }

        // 3. Kiểm tra tính hợp lệ của giá đặt
        if (amount <= this.currentHighestBid) {
            throw new RuntimeException("Lỗi: Giá đặt (" + amount + ") phải lớn hơn giá hiện tại (" + this.currentHighestBid + ")");
        }

        // 4. Cập nhật dữ liệu người dẫn đầu
        this.currentHighestBid = amount;
        this.highestBidder = bidder;

        // 5. Lưu biên lai vào lịch sử
        BidTransaction transaction = new BidTransaction(this, bidder, amount);
        this.bidHistory.add(transaction);

        System.out.println("=> Hợp lệ: [" + bidder.getUsername() + "] đã dẫn đầu với giá " + amount);
        return true;
    }

    
    // CHỨC NĂNG 3.1.4: KẾT THÚC PHIÊN ĐẤU GIÁ

    public void closeAuction() {
        // Tránh trường hợp bị gọi đóng nhiều lần
        if (this.status == AuctionStatus.FINISHED || this.status == AuctionStatus.PAID || this.status == AuctionStatus.CANCELED) {
            return;
        }

        this.status = AuctionStatus.FINISHED;
        System.out.println("\n--- TỔNG KẾT PHIÊN ĐẤU GIÁ [" + item.getName() + "] ---");

        if (highestBidder != null) {
            System.out.println("Người thắng cuộc: " + highestBidder.getUsername());
            System.out.println("Giá chốt: " + currentHighestBid);

            // Chuyển sang trạng thái chờ thanh toán
            this.status = AuctionStatus.PAID;
            System.out.println("Trạng thái cuối: " + this.status);
        } else {
            System.out.println("Không có ai tham gia trả giá.");

            // Hủy phiên vì ế
            this.status = AuctionStatus.CANCELED;
            System.out.println("Trạng thái cuối: " + this.status);
        }
    }

    // --- Các hàm Getters để lấy dữ liệu ra xem ---
    public AuctionStatus getStatus() { return status; }
    public double getCurrentHighestBid() { return currentHighestBid; }
    public Bidder getHighestBidder() { return highestBidder; }
    public List<BidTransaction> getBidHistory() { return bidHistory; }
    public Item getItem() { return item; }
}