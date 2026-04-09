package com.auction.server;


import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Antique;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.auction.BidTransaction;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        System.out.println("====== HỆ THỐNG ĐẤU GIÁ BẮT ĐẦU ======\n");

        // ---------------------------------------------------------
        // BƯỚC 1: KHỞI TẠO NGƯỜI DÙNG (NHƯNG KHÔNG NẠP TIỀN)
        // ---------------------------------------------------------
        System.out.println("[1] Đang khởi tạo dữ liệu người dùng...");
        Seller sellerJohn = new Seller("john_doe", "pass123", "John Đỗ", "S-001");

        Bidder bidderAlice = new Bidder("alice99", "pass123", "Alice Trần", "B-001", "dffadf");
        Bidder bidderBob = new Bidder("bob_rich", "pass123", "Bob Nguyễn", "B-002", "dffadf");

        // CỐ TÌNH KHÔNG NẠP TIỀN ĐỂ TEST XEM HỆ THỐNG CÓ BỊ LỖI KHÔNG
        bidderAlice.deposit(5000.0);
        bidderBob.deposit(10000.0);
        bidderBob.deposit(10000.0);
        // ---------------------------------------------------------
        // BƯỚC 2: TẠO MẶT HÀNG ĐỂ BÁN
        // ---------------------------------------------------------
        System.out.println("\n[2] Đang thẩm định mặt hàng...");
        Antique vase = new Antique(
                "Bình gốm thời nhà Minh",
                "Tuyệt tác gốm sứ cổ đại",
                500.0, 1, "Mint",
                "Nhà Minh - Thế kỷ 15", "Gốm sứ", true
        );

        // ---------------------------------------------------------
        // BƯỚC 3 & 4: TẠO VÀ MỞ PHIÊN ĐẤU GIÁ
        // ---------------------------------------------------------
        System.out.println("[3] Đưa mặt hàng lên sàn...");
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plusMinutes(5);

        Auction auctionRoom = sellerJohn.createAuction(vase, startTime, endTime);
        auctionRoom.startAuction();

        // ---------------------------------------------------------
        // BƯỚC 5: CÁC ĐẠI GIA TRANH NHAU TRẢ GIÁ (KHÔNG CÓ TIỀN VẪN HÔ TO)
        // ---------------------------------------------------------
        System.out.println("\n--- DIỄN BIẾN SÀN ĐẤU GIÁ ---");
        try {
            auctionRoom.processBid(bidderAlice, 600.0);
            auctionRoom.processBid(bidderBob, 800.0);
            auctionRoom.processBid(bidderAlice, 1500.0);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        // ---------------------------------------------------------
        // BƯỚC 6: ĐÓNG SÀN CHỐT ĐƠN (HỆ THỐNG SẼ PHÁT HIỆN ALICE KHÔNG CÓ TIỀN)
        // ---------------------------------------------------------
        System.out.println("\n*** HẾT GIỜ! ĐÓNG PHIÊN ĐẤU GIÁ ***");
        auctionRoom.closeAuction();

        // =========================================================
        // BƯỚC 7: XUẤT BÁO CÁO TOÀN DIỆN
        // =========================================================
        System.out.println("\n=======================================================");
        System.out.println("             BÁO CÁO TOÀN TRẠNG HỆ THỐNG             ");
        System.out.println("=======================================================");

        // 1. IN LỊCH SỬ ĐẤU GIÁ
        System.out.println("\n[1] LỊCH SỬ PHIÊN ĐẤU GIÁ");
        System.out.println("Trạng thái phiên: " + auctionRoom.getStatus());
        System.out.println("Các lượt trả giá:");


        // 2. IN THÔNG TIN NGƯỜI BÁN
        System.out.println("\n[2] TÌNH TRẠNG NGƯỜI BÁN (SELLER)");
        System.out.println("Tên người bán: " + sellerJohn.getFullName());
        System.out.println("Số dư ví hiện tại: " + sellerJohn.getBalance() + "$");

        // 3. IN THÔNG TIN NGƯỜI MUA
        System.out.println("\n[3] TÌNH TRẠNG TÀI KHOẢN NGƯỜI MUA (BIDDERS)");
        System.out.println("- Khách hàng [" + bidderAlice.getUsername() + "]:");
        System.out.println("  + Số dư hiện tại: " + bidderAlice.getBalance() + "$");

        System.out.println("- Khách hàng [" + bidderBob.getUsername() + "]:");
        System.out.println("  + Số dư hiện tại: " + bidderBob.getBalance() + "$");

        System.out.println("\n================ KẾT THÚC CHƯƠNG TRÌNH ================");
    }
}