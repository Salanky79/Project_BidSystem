package com.auction.share.models.auction;

import com.auction.share.enums.AuctionStatus;
import com.auction.share.models.core.Entity;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;

import java.time.LocalDateTime;

/**
 * Thông tin phiên đấu giá và trạng thái hiện tại.
 */
public class Auction extends Entity {
    private Item item;
    private Seller seller;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double currentHighestBid;
    private Bidder highestBidder;
    private double bidStep;
    private AuctionStatus status;

    /**
     * Tạo phiên đấu giá từ cấu hình sản phẩm và thời gian.
     */
    public Auction(Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime) {
        super();
        this.item = item;
        this.seller = seller;
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentHighestBid = item.getStartingPrice();
        this.highestBidder = null;
        this.bidStep = 1.0d;
        this.status = AuctionStatus.OPEN;
    }

    public void markDraft() {
        this.status = AuctionStatus.DRAFT;
    }

    public void markRunning() {
        this.status = AuctionStatus.RUNNING;
    }

    /**
     * Cập nhật mức giá cao nhất và người đang dẫn đầu.
     */
    public void setHighestBid(Bidder bidder, double amount) {
        this.currentHighestBid = amount;
        this.highestBidder = bidder;
    }

    public void markFinished() {
        this.status = AuctionStatus.FINISHED;
    }

    public void markCanceled() {
        this.status = AuctionStatus.CANCELED;
    }

    /**
     * Gia hạn thời gian kết thúc theo phút.
     */
    public void extendEndTimeMinutes(long minutes) {
        this.endTime = this.endTime.plusMinutes(minutes);
    }

    public Seller getSeller() { return seller; }
    public AuctionStatus getStatus() { return status; }
    public double getCurrentHighestBid() { return currentHighestBid; }
    public Bidder getHighestBidder() { return highestBidder; }
    public double getBidStep() { return bidStep; }
    public Item getItem() { return item; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }

    public void setBidStep(double bidStep) { this.bidStep = bidStep; }
}
