package com.auction.share.models.auction;

import com.auction.share.enums.AuctionStatus;
import com.auction.share.models.core.Entity;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;

import java.time.LocalDateTime;

/**
 * Đại diện cho một Phiên đấu giá trong hệ thống.
 * Chứa thông tin về sản phẩm, người bán, thời gian bắt đầu/kết thúc
 * cũng như trạng thái đấu giá và thông tin người trả giá cao nhất hiện tại.
 */
public class Auction extends Entity {
    /**
     * Sản phẩm đang được đưa ra đấu giá.
     */
    private Item item;

    /**
     * Người bán khởi tạo phiên đấu giá.
     */
    private Seller seller;

    /**
     * Thời gian dự kiến bắt đầu phiên đấu giá.
     */
    private LocalDateTime startTime;

    /**
     * Thời gian dự kiến kết thúc phiên đấu giá.
     */
    private LocalDateTime endTime;

    /**
     * Mức giá cao nhất hiện tại trong phiên. Ban đầu bằng giá khởi điểm của sản phẩm.
     */
    private double currentHighestBid;

    /**
     * Người trả giá (Bidder) đang giữ mức giá cao nhất.
     */
    private Bidder highestBidder;
    private double bidStep;

    /**
     * Trạng thái hiện tại của phiên đấu giá (OPEN, RUNNING, FINISHED, CANCELED).
     */
    private AuctionStatus status;

    /**
     * Khởi tạo một phiên đấu giá.
     * Mặc định mức giá cao nhất bằng giá khởi điểm, chưa có người trả giá và trạng thái là OPEN.
     *
     * @param item      Sản phẩm đấu giá
     * @param seller    Người bán
     * @param startTime Thời gian bắt đầu
     * @param endTime   Thời gian kết thúc
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

    /**
     * Đánh dấu phiên đấu giá là bản nháp.
     */
    public void markDraft() {
        this.status = AuctionStatus.DRAFT;
    }

    /**
     * Đánh dấu phiên đấu giá đang trong quá trình diễn ra.
     */
    public void markRunning() {
        this.status = AuctionStatus.RUNNING;
    }

    /**
     * Cập nhật mức trả giá cao nhất mới và ghi nhận người đấu giá.
     *
     * @param bidder Người trả giá
     * @param amount Mức giá mới
     */
    public void setHighestBid(Bidder bidder, double amount) {
        this.currentHighestBid = amount;
        this.highestBidder = bidder;
    }

    /**
     * Đánh dấu phiên đấu giá đã kết thúc thành công.
     */
    public void markFinished() {
        this.status = AuctionStatus.FINISHED;
    }

    /**
     * Đánh dấu phiên đấu giá bị hủy bỏ.
     */
    public void markCanceled() {
        this.status = AuctionStatus.CANCELED;
    }

    /**
     * Gia hạn thêm thời gian kết thúc phiên đấu giá.
     * Thường dùng khi có người trả giá vào những phút cuối cùng.
     *
     * @param minutes Số phút cần gia hạn thêm
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
