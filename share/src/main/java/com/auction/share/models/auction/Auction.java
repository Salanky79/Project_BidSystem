package com.auction.share.models.auction;

import com.auction.share.models.user.Seller;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.item.Item;
import com.auction.share.models.core.Entity;
import com.auction.share.enums.AuctionStatus;

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

    public Auction(Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime) {
        super();
        this.item = item;
        this.seller = seller;
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentHighestBid = item.getStartingPrice();
        this.highestBidder = null;
        this.status = AuctionStatus.OPEN;
        this.bidHistory = new ArrayList<>();
    }

    public void startAuction() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            System.out.println("Chưa tới giờ bắt đầu phiên đấu giá!");
            return;
        }
        this.status = AuctionStatus.RUNNING;
        System.out.println("\n[HỆ THỐNG] Phiên đấu giá cho [" + item.getName() + "] BẮT ĐẦU!");
    }

    public synchronized boolean processBid(Bidder bidder, double amount) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(startTime)) {
            throw new RuntimeException("Lỗi: Phiên đấu giá chưa tới giờ bắt đầu!");
        }
        if (now.isAfter(endTime)) {
            closeAuction();
            throw new RuntimeException("Lỗi: Phiên đấu giá đã kết thúc vào lúc " + endTime);
        }

        if (this.status != AuctionStatus.RUNNING) {
            throw new RuntimeException("Lỗi: Phiên đấu giá không ở trạng thái hoạt động!");
        }

        if (amount <= this.currentHighestBid) {
            throw new RuntimeException("Lỗi: Giá đặt (" + amount + ") phải lớn hơn giá hiện tại (" + this.currentHighestBid + ")");
        }

        this.currentHighestBid = amount;
        this.highestBidder = bidder;

        BidTransaction transaction = new BidTransaction(this, bidder, amount);
        this.bidHistory.add(transaction);

        System.out.println("=> Hợp lệ: [" + bidder.getUsername() + "] đã dẫn đầu với giá " + amount);
        return true;
    }

    public void closeAuction() {
        this.status = AuctionStatus.PAID;
        System.out.println("\n--- TỔNG KẾT PHIÊN ĐẤU GIÁ [" + item.getName() + "] ---");

        if (highestBidder != null) {
            System.out.println("Người thắng cuộc: " + highestBidder.getUsername());
            System.out.println("Giá chốt: " + currentHighestBid);
            this.status = AuctionStatus.PAID;
            System.out.println("Trạng thái cuối: " + this.status);
        } else {
            System.out.println("Không có ai tham gia trả giá.");
            this.status = AuctionStatus.PAID;
            System.out.println("Trạng thái cuối: " + this.status);
        }
    }

    public AuctionStatus getStatus() { return status; }
    public double getCurrentHighestBid() { return currentHighestBid; }
    public Bidder getHighestBidder() { return highestBidder; }
    public List<BidTransaction> getBidHistory() { return bidHistory; }
    public Item getItem() { return item; }
}

