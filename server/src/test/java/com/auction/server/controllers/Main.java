import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        System.out.println("===== DEMO HỆ THỐNG ĐẤU GIÁ (Bám sát Đặc tả) =====\n");

        // 1. Quản lý sản phẩm (Đã thêm Description)
        Item item = new Item("MacBook Pro M3", "Bản 16GB/512GB, mới 100%", 30000000);
        Seller seller = new Seller("fpt_shop", "123", "FPT Shop Official");

        Bidder bidderA = new Bidder("nguoi_mua_A", "pass1", 50000000);
        Bidder bidderB = new Bidder("nguoi_mua_B", "pass2", 50000000);

        // 2. Thiết lập thời gian: Bắt đầu ngay bây giờ, KẾT THÚC TRONG 2 GIÂY NỮA
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plusSeconds(2);

        Auction auction = new Auction(item, seller, startTime, endTime);

        // --- BẮT ĐẦU KỊCH BẢN ---
        auction.startAuction();

        try {
            // A đặt giá hợp lệ
            System.out.println("\n[Lượt 1] Anh A đặt 31 triệu...");
            auction.processBid(bidderA, 31000000);

            // B tranh giành, đặt cao hơn
            System.out.println("\n[Lượt 2] Anh B đặt 32 triệu...");
            auction.processBid(bidderB, 32000000);

            // A cố tình đặt giá thấp hơn B để xem hệ thống bắt lỗi không
            System.out.println("\n[Lượt 3] Anh A cố tình đặt 31.5 triệu (thấp hơn giá hiện tại của B)...");
            auction.processBid(bidderA, 31500000);

        } catch (RuntimeException e) {
            System.out.println("-> HỆ THỐNG CHẶN: " + e.getMessage());
        }

        // 3. Test tính năng TỰ ĐỘNG ĐÓNG PHIÊN KHI HẾT GIỜ (Yêu cầu 3.1.4)
        System.out.println("\n⏳ Đang chờ 3 giây để mô phỏng hết thời gian đấu giá...");
        try {
            Thread.sleep(3000); // Bắt chương trình tạm dừng 3 giây
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            // Sau 3 giây (đã lố giờ endTime), B hăng máu muốn đặt thêm 35 triệu
            System.out.println("\n[Lượt 4] Anh B muốn chốt giá 35 triệu nhưng đã quá giờ...");
            auction.processBid(bidderB, 35000000);
        } catch (RuntimeException e) {
            System.out.println("-> HỆ THỐNG CHẶN: " + e.getMessage());
        }

        // 4. In ra lịch sử giao dịch (Biên lai)
        System.out.println("\n=================================");
        System.out.println("LỊCH SỬ GIAO DỊCH (BID HISTORY):");
        for (BidTransaction tx : auction.getBidHistory()) {
            System.out.println("- [" + tx.getTimestamp().toLocalTime() + "] " +
                    tx.getBidder().getUsername() + " đặt " + tx.getAmount() + " VNĐ");
        }
    }
}