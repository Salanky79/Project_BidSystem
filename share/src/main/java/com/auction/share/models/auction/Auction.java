package com.auction.share.models.auction;

import com.auction.share.enums.AuctionStatus;
import com.auction.share.exceptions.*;
import com.auction.share.models.core.Entity;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;

import java.time.Duration;
import java.time.LocalDateTime;

public class Auction extends Entity {
    private Item item;
    private Seller seller;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private double currentHighestBid;
    private Bidder highestBidder;
    private AuctionStatus status;

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

    // -------------------------------------------------------------
    // CHỨC NĂNG (mục 3.1.3): THAM GIA ĐẤU GIÁ
    // -------------------------------------------------------------

    // Người dùng bấm nút đấu giá
    // -------------------------------------------------------------
    // CHỨC NĂNG (mục 3.1.3): THAM GIA ĐẤU GIÁ
    // -------------------------------------------------------------
    public synchronized boolean processBid(Bidder bidder, double amount) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Kiểm tra thời gian
        if (now.isBefore(startTime)) {
            throw new AuctionNotStartedException(
                "Phiên đấu giá [" + item.getName() + "] chưa bắt đầu. " +
                "Thời gian bắt đầu: " + startTime
            );
        }

        if (now.isAfter(endTime)) {
            closeAuction();
            throw new AuctionClosedException(
                "Phiên đấu giá [" + item.getName() + "] đã kết thúc vào lúc " + endTime
            );
        }

        // 2. Kiểm tra trạng thái
        if (this.status != AuctionStatus.RUNNING) {
            throw new AuctionNotRunningException(
                "Phiên đấu giá [" + item.getName() + "] không ở trạng thái hoạt động. " +
                "Trạng thái hiện tại: " + status
            );
        }

        // 3. Kiểm tra tính hợp lệ của giá đặt
        if (amount <= 0) {
            throw new InvalidBidException("Giá đặt không hợp lệ!");
        }

        if (amount <= this.currentHighestBid) {
            throw new BidTooLowException(
                    "Giá đặt (" + amount + "$) phải cao hơn giá hiện tại (" + currentHighestBid + "$)"
            );
        }

        // 4. Kiểm tra số dư
        if (bidder.getBalance() < amount) {
            throw new InsufficientFundsException(
                "Bidder [" + bidder.getUsername() + "] không đủ tiền! " +
                "Số dư: " + bidder.getBalance() + "$, yêu cầu: " + amount + "$"
            );
        }

        // 5. Cập nhật dữ liệu người dẫn đầu
        this.currentHighestBid = amount;
        this.highestBidder = bidder;


        // 7. Gia hạn thời gian đấu giá
        long secondsLeft = Duration.between(LocalDateTime.now(), endTime).getSeconds();
        if (secondsLeft > 0 && secondsLeft < 30) { // Nếu còn dưới 30s
            this.endTime = this.endTime.plusMinutes(1); // Gia hạn thêm 1 phút
            System.out.println("Phiên đấu giá được gia hạn thêm 1 phút do có lượt đặt giá cuối!");
        }

        System.out.println("=> Hợp lệ: [" + bidder.getUsername() + "] đã dẫn đầu với giá " + amount + "$");
        return true;
    }

    // -------------------------------------------------------------
    // CHỨC NĂNG 3.1.4: KẾT THÚC PHIÊN ĐẤU GIÁ (CÓ THANH TOÁN)
    // -------------------------------------------------------------
    public void closeAuction() {
        // Tránh trường hợp bị gọi đóng nhiều lần
        if (this.status == AuctionStatus.FINISHED || this.status == AuctionStatus.CANCELED) {
            return;
        }

        this.status = AuctionStatus.FINISHED;
        System.out.println("\n--- TỔNG KẾT PHIÊN ĐẤU GIÁ [" + item.getName() + "] ---");

        if (highestBidder != null) {
            System.out.println("Người thắng cuộc: " + highestBidder.getUsername());
            System.out.println("Giá chốt: " + currentHighestBid + "$");

            // --- BẮT ĐẦU XỬ LÝ THANH TOÁN ---
            try {
                // 1. Trừ tiền người mua (Bidder)
                highestBidder.deductBalance(currentHighestBid);
                System.out.println("[-] Đã trừ " + currentHighestBid + "$ từ tài khoản người mua [" + highestBidder.getUsername() + "]");

                // 2. Cộng tiền cho người bán (Seller)
                this.seller.addBalance(currentHighestBid);

                // 3. Chuyển sang trạng thái đã thanh toán thành công
                this.status = AuctionStatus.CANCELED;
                System.out.println("Trạng thái cuối: " + this.status);

            } catch (Exception e) {
                // Nếu hàm deductBalance báo lỗi (không đủ tiền)
                System.out.println("Lỗi Thanh Toán: " + e.getMessage());
                this.status = AuctionStatus.CANCELED; // Hủy kết quả
                System.out.println("Trạng thái cuối: " + this.status + " (Do người mua không đủ tiền)");
            }
            // --- KẾT THÚC XỬ LÝ THANH TOÁN ---

        } else {
            System.out.println("Không có ai tham gia trả giá.");

            // Hủy phiên vì không có ai mua
            this.status = AuctionStatus.CANCELED;
            System.out.println("Trạng thái cuối: " + this.status);
        }
    }

    // --- Các hàm Getters để lấy dữ liệu ra xem ---
    public AuctionStatus getStatus() { return status; }
    public double getCurrentHighestBid() { return currentHighestBid; }
    public Bidder getHighestBidder() { return highestBidder; }
    public Item getItem() { return item; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}