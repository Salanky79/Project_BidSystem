package com.auction.share.DTO;

import java.io.Serializable;
import java.util.List;

/**
 * DTO chi tiết phiên đấu giá dùng cho màn hình xem chi tiết.
 */
public class AuctionDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String itemName;
    private final String description;
    private final String category;
    private final String sellerName;
    private final double startingPrice;
    private final double currentPrice;
    private final String status;
    private final String startTime;
    private final String endTime;
    private final String highestBidderName;
    private final List<BidDTO> bidHistory;

    public AuctionDetailDTO(
            String auctionId,
            String itemName,
            String description,
            String category,
            String sellerName,
            double startingPrice,
            double currentPrice,
            String status,
            String startTime,
            String endTime,
            String highestBidderName,
            List<BidDTO> bidHistory
    ) {
        this.auctionId = auctionId;
        this.itemName = itemName;
        this.description = description;
        this.category = category;
        this.sellerName = sellerName;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.highestBidderName = highestBidderName;
        this.bidHistory = bidHistory;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public String getStatus() {
        return status;
    }
}