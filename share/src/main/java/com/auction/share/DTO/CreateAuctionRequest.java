package com.auction.share.DTO;

public class CreateAuctionRequest extends Request {
    private static final long serialVersionUID = 1L;

    private final String sellerId;
    private final String itemName;
    private final String description;
    private final String category;
    private final double startingPrice;
    private final String startTime;
    private final String endTime;

    public CreateAuctionRequest(
            String sellerId,
            String itemName,
            String description,
            String category,
            double startingPrice,
            String startTime,
            String endTime
    ) {
        super(Action.CREATE_AUCTION);
        this.sellerId = sellerId;
        this.itemName = itemName;
        this.description = description;
        this.category = category;
        this.startingPrice = startingPrice;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getItemName() {
        return itemName;
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

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    @Override
    public Request withUserId(String userId) {
        if (this.sellerId == null || this.sellerId.isBlank()) {
            return new CreateAuctionRequest(
                    userId,
                    itemName,
                    description,
                    category,
                    startingPrice,
                    startTime,
                    endTime
            );
        }
        return this;
    }
}
