package com.auction.client.controller;

import com.auction.share.DTO.AuctionDetailDTO;
import com.auction.share.DTO.BidDTO;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.client.utils.DateTimeUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel quản lý trạng thái dữ liệu (State) của màn hình chi tiết đấu giá.
 * Đóng vai trò là cầu nối giữa DTO nhận từ mạng (Model) và Giao diện hiển thị (View/Controller).
 */
public class AuctionDetailViewModel {
    // ── THÔNG TIN CHI TIẾT PHIÊN ĐẤU GIÁ ──────────────────────────────
    private String auctionId;         // Mã định danh duy nhất của phiên đấu giá
    private String name;              // Tên sản phẩm đấu giá
    private String icon;              // Ký tự biểu tượng sản phẩm (Emoji)
    private double currentPrice;      // Giá cao nhất hiện tại của phiên
    private double bidStep;           // Bước giá tối thiểu bắt buộc giữa các lần thầu
    private int totalBids;            // Tổng số lượt đã đặt giá thầu
    private LocalDateTime endTime;    // Thời gian kết thúc phiên đấu giá
    private String startTimeISO;      // Thời gian bắt đầu phiên đấu giá (chuỗi ISO)
    private double startingPrice;     // Giá khởi điểm ban đầu
    private String sellerName;        // Tên người bán hàng (chủ phiên)
    private String description;       // Mô tả chi tiết về sản phẩm
    private String imageUrl;          // Đường dẫn ảnh sản phẩm đấu giá
    private String status;            // Trạng thái phiên (RUNNING, FINISHED, CANCELED)
    private String highestBidderName; // Tên người đang giữ giá cao nhất hiện tại

    // ── TRẠNG THÁI NGƯỜI DÙNG HIỆN TẠI ───────────────────────────────
    private boolean autoBidEnabled;   // Trạng thái bật/tắt tự động đấu giá của người dùng hiện tại

    // ── LỊCH SỬ GIÁ THẦU ─────────────────────────────────────────────
    private List<BidDTO> bidHistory = new ArrayList<>(); // Danh sách toàn bộ các lượt thầu trước đó
    
    /**
     * Bộ đếm lượt đặt thầu cuối cùng đã được xử lý thành công.
     * Sử dụng làm bộ lọc kiểm soát đồng bộ mạng: ngăn các sự kiện cũ/trễ ghi đè lên dữ liệu mới hơn.
     */
    private int lastProcessedBidCount = -1;

    /**
     * Khởi tạo các dữ liệu ban đầu cơ bản khi vừa mở màn hình chi tiết.
     * Dữ liệu này được lấy từ dòng hiển thị ở danh sách trước khi truy vấn chi tiết từ server.
     */
    public void initData(
            String icon,
            String category,
            String name,
            double price,
            double bidStep,
            int bids,
            String time,
            String status,
            String auctionId
    ) {
        this.icon = icon;
        this.name = name;
        this.currentPrice = price;
        this.bidStep = bidStep;
        this.totalBids = bids;
        this.status = status;
        this.auctionId = auctionId;
        this.endTime = DateTimeUtils.parseDateTime(time);
        this.autoBidEnabled = false;
    }

    /**
     * Cập nhật toàn bộ trạng thái dữ liệu chi tiết từ DTO nhận về từ Server.
     * Thường dùng khi màn hình được tải lần đầu hoặc khi người dùng làm mới (Refresh).
     */
    public void updateFrom(AuctionDetailDTO detail) {
        if (detail == null) return;
        this.currentPrice = detail.getCurrentPrice();
        this.bidStep = detail.getBidStep();
        this.bidHistory = detail.getBidHistory() != null ? detail.getBidHistory() : new ArrayList<>();
        this.totalBids = this.bidHistory.size();
        this.startTimeISO = detail.getStartTime();
        this.startingPrice = detail.getStartingPrice();
        this.sellerName = detail.getSellerName();
        this.description = detail.getDescription() != null ? detail.getDescription() : "No description.";
        this.lastProcessedBidCount = detail.getBidCount();
        this.imageUrl = detail.getImageUrl();
        this.status = detail.getStatus();
        this.highestBidderName = detail.getHighestBidderName();
        
        if (detail.getEndTime() != null) {
            this.endTime = DateTimeUtils.parseDateTime(detail.getEndTime());
        }
    }

    /**
     * Áp dụng sự kiện cập nhật giá thầu mới nhận được thời gian thực từ Socket (tin nhắn Push).
     * 
     * @param event Đối tượng chứa thông tin thầu mới đẩy về từ server.
     * @return true nếu dữ liệu hợp lệ và giao diện cần được vẽ lại, false nếu bỏ qua do tin trùng/lỗi.
     */
    public boolean applyBidUpdate(BidUpdateEvent event) {
        if (event == null) return false;
        
        // 1. Kiểm soát thứ tự gói tin: Bỏ qua nếu sự kiện này là cũ hơn sự kiện đã xử lý
        if (event.getBidCount() <= lastProcessedBidCount) {
            return false; 
        }
        
        this.lastProcessedBidCount = event.getBidCount();
        this.currentPrice = event.getCurrentHighestBid();
        this.highestBidderName = event.getBidderName();

        if (this.bidHistory == null) {
            this.bidHistory = new ArrayList<>();
        }

        // 2. Kiểm tra trùng lặp: Đảm bảo thầu này chưa tồn tại trong danh sách vẽ biểu đồ
        boolean exists = false;
        for (BidDTO b : this.bidHistory) {
            if (Double.compare(b.getAmount(), event.getAmount()) == 0
                    && b.getBidderName().equals(event.getBidderName())
                    && b.getTimestamp().equals(event.getBidTime())) {
                exists = true;
                break;
            }
        }

        // 3. Nếu thầu mới tinh, lưu lịch sử thầu và thông báo cập nhật UI thành công
        if (!exists) {
            this.bidHistory.add(new BidDTO(event.getBidderName(), event.getAmount(), event.getBidTime()));
            this.totalBids = this.bidHistory.size();
            return true;
        }
        return false;
    }

    /**
     * Thuộc tính tự tính toán (Computed Property): 
     * Xác định mức giá tối thiểu bắt buộc người mua phải nhập để hợp lệ cho lượt đặt tiếp theo.
     * Giá tối thiểu = Giá cao nhất hiện tại + Bước giá quy định.
     */
    public double getMinimumBid() {
        return currentPrice + bidStep;
    }

    /**
     * Thuộc tính tự tính toán (Computed Property):
     * Kiểm tra xem phiên đấu giá hiện tại đã kết thúc hay chưa.
     * Kết thúc khi trạng thái là FINISHED/CANCELED hoặc thời gian hiện hành vượt quá thời gian kết thúc.
     */
    public boolean isEnded() {
        return "FINISHED".equals(status)
                || "CANCELED".equals(status)
                || (endTime != null && LocalDateTime.now().isAfter(endTime));
    }

    // ── GETTERS VÀ SETTERS CỦA VIEWMODEL ──────────────────────────────
    
    public String getAuctionId() { return auctionId; }
    public void setAuctionId(String auctionId) { this.auctionId = auctionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public double getBidStep() { return bidStep; }
    public void setBidStep(double bidStep) { this.bidStep = bidStep; }

    public int getTotalBids() { return totalBids; }
    public void setTotalBids(int totalBids) { this.totalBids = totalBids; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStartTimeISO() { return startTimeISO; }
    public void setStartTimeISO(String startTimeISO) { this.startTimeISO = startTimeISO; }

    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }

    public boolean isAutoBidEnabled() { return autoBidEnabled; }
    public void setAutoBidEnabled(boolean autoBidEnabled) { this.autoBidEnabled = autoBidEnabled; }

    public List<BidDTO> getBidHistory() { return bidHistory; }
    public void setBidHistory(List<BidDTO> bidHistory) { this.bidHistory = bidHistory; }

    public int getLastProcessedBidCount() { return lastProcessedBidCount; }
    public void setLastProcessedBidCount(int lastProcessedBidCount) { this.lastProcessedBidCount = lastProcessedBidCount; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getHighestBidderName() { return highestBidderName; }
    public void setHighestBidderName(String highestBidderName) { this.highestBidderName = highestBidderName; }
}
