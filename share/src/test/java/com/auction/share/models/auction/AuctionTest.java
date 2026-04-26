package com.auction.share.models.auction;

import com.auction.share.enums.AuctionStatus;
import com.auction.share.exceptions.*;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionTest {
    private Auction auction;
    private Seller seller;
    private Bidder bidder1, bidder2;
    private Item item;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Chuẩn bị dữ liệu cho mỗi test
    @BeforeEach
    public void setUp() {
        // Tạo người bán
        seller = new Seller("seller", "password", "Người bán");

        // Tạo 2 người mua
        bidder1 = new Bidder("bidder1", "password", "Người mua 1", "Hà Nội");
        bidder2 = new Bidder("bidder2", "password", "Người mua 2", "Thanh Hóa");

        bidder1.deposit(5000);   // Nạp tiền cho bidder1
        bidder2.deposit(10000);  // Nạp tiền cho bidder2

        item = new Item("Đồng hồ cổ", "Đồng hồ cổ đẹp", 1000, seller.getId());

        // Tạo phiên đấu giá: bắt đầu ngay và kết thúc sau 1 giờ
        startTime = LocalDateTime.now();
        endTime = LocalDateTime.now().plusHours(1);
        auction = new Auction(item, seller, startTime, endTime);

        auction.startAuction();
    }

    @Test
    public void testProcessBidSuccess() {
        // Bidder1 đặt giá 1500
        assertDoesNotThrow(() -> auction.processBid(bidder1, 1500));
        assertEquals(1500, auction.getCurrentHighestBid());
        assertEquals(bidder1.getId(), auction.getHighestBidder().getId());

        // Bidder2 đặt giá 2000
        assertDoesNotThrow(() -> auction.processBid(bidder2, 2000));
        assertEquals(2000, auction.getCurrentHighestBid());
        assertEquals(bidder2.getId(), auction.getHighestBidder().getId());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, 0})
    public void testInvalidBidAmounts(double amount) {
        assertThrows(InvalidBidException.class, () -> auction.processBid(bidder1, amount));
    }
    @Test
    public void testProcessBidNegativeBid() {
        assertThrows(InvalidBidException.class, () -> auction.processBid(bidder1, -0.01));
    }

    // EP CLASS 2: amount = 0 (Boundary)
    @Test
    public void testProcessBidWithZeroBid() {
        assertThrows(InvalidBidException.class, () -> auction.processBid(bidder1, 0));
    }

    // EP CLASS 3: amount ≤ currentHighestBid
    @ParameterizedTest
    @ValueSource(doubles = {1400, 1500})
    public void testProcessBidLowerThanCurrent(double amount) {
        auction.processBid(bidder1, 1500);
        assertThrows(BidTooLowException.class, () -> auction.processBid(bidder2, amount));
    }

    @Test
    public void testProcessBidEqualCurrent() {
        assertThrows(InvalidBidException.class, () -> auction.processBid(bidder1, 1000));
    }

    @Test
    public void testProcessBidInsufficientBid() {
        assertThrows(InsufficientFundsException.class, () -> auction.processBid(bidder1, 6000));
    }

    @Test
    public void testProcessBidBeforeAuctionStart() {
        LocalDateTime futureStart = LocalDateTime.now().plusHours(1);
        LocalDateTime futureEnd = LocalDateTime.now().plusHours(2);
        Auction futureAuction = new Auction(item, seller, futureStart, futureEnd);

        assertThrows(AuctionNotStartedException.class, () -> futureAuction.processBid(bidder1, 1500));
    }

    @Test
    public void testProcessBidAfterAuctionEnd() {
        LocalDateTime passStart = LocalDateTime.now().minusHours(2);
        LocalDateTime passEnd = LocalDateTime.now().plusHours(1);
        Auction passAuction = new Auction(item, seller, passStart, passEnd);

        assertThrows(AuctionClosedException.class, () -> passAuction.processBid(bidder1, 1500));
    }

    @Test
    public void testStartAuctionSuccess() {
        assertEquals(AuctionStatus.RUNNING, auction.getStatus());
    }

    //Test edge case: Phiên đóng trong lúc có lệnh bid cuối
    @Test
    public void testCloseAuctionWithNoWinner() {
        auction.closeAuction();

        assertEquals(AuctionStatus.CANCELED, auction.getStatus());
        assertNull(auction.getHighestBidder());
        assertEquals(0.0, seller.getBalance());
    }

    @Test
    public void testBidWithAmountBoundaryAtTimeBoundary() {
        // Đặt số tiền đúng bằng balance
        boolean result = auction.processBid(bidder1, 5000.0);
        assertTrue(result, "BVA: Giá = balance ở valid time");
    }
}