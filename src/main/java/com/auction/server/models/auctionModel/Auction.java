public class Auction {
    private String auctionId;
    private Item item;
    private Seller seller;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double currentHighestPrice;
    private Bidder highestBidder;
    private String status; //Open/running/finish

    public void startAuction() {
        //Khoi dong phien//
    }
    public void closeAuction() {
        //Ket thuc phien//
    }
    public boolean processBid(bidder, amount) {
        //Neu dung dieu kien -> true -> thay doi gia
        //Neu sai dieu kien -> false -> bao Loi
    }
}