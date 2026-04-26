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

    @BeforeEach
    public void setUp() {
        seller = new Seller("seller", "password", "Người bán");
        bidder1 = new Bidder("bidder1", "password", "Người mua 1", "Hà Nội");
        bidder2 = new Bidder("bidder2", "password", "Người mua 2", "Thanh Hóa");

        bidder1.deposit(5000);   // bidder1 có 5000$
        bidder2.deposit(10000);  // bidder2 có 10000$

        item = new Item("Đồng hồ cổ", "Đồng hồ cổ đẹp", 1000, seller.getId());

        LocalDateTime startTime = LocalDateTime.now().minusMinutes(5);
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);
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

    // amount ≤ 0
    @ParameterizedTest
    @ValueSource(doubles = {-100.0, -0.01, 0.0})
    public void testNegativeOrZeroBidAmounts(double amount) {
        assertThrows(InvalidBidException.class, () -> auction.processBid(bidder1, amount));
    }

    // amount ≤ currentHighestBid (1000)
    @ParameterizedTest
    @ValueSource(doubles = {0.01, 500.0, 999.99, 1000.0}) // Gồm cả on boundary
    public void testProcessBidBelowOrEqualCurrentHighestBid(double amount) {
        assertThrows(BidTooLowException.class, () -> auction.processBid(bidder1, amount));
    }

    // balance (5000) >= amount > currentHighestBid (1000)
    @ParameterizedTest
    @ValueSource(doubles = {1000.01, 2500, 5000})
    public void testProcessBidAboveCurrentBid(double amount) {
        auction.processBid(bidder1, amount);
        assertEquals(amount, auction.getCurrentHighestBid(), 0.01);
    }

    // amount > balance (5000)
    @ParameterizedTest
    @ValueSource(doubles = {5000.01, 10000.0})
    public void testProcessBidAboveBalance(double amount) {
        assertThrows(InsufficientFundsException.class, () -> auction.processBid(bidder1, amount));
    }

    // Thời gian < startTime (chưa bắt đầu)
    @ParameterizedTest
    @ValueSource(longs = {1, 60, 3600}) // 1s, 1 phút, 1 giờ
    public void testProcessBidBeforeAuctionStart(long amount) {
        LocalDateTime futureStart = LocalDateTime.now().plusSeconds(amount);
        LocalDateTime futureEnd = LocalDateTime.now().plusSeconds(amount + 3600);
        Auction futureAuction = new Auction(item, seller, futureStart, futureEnd);

        assertEquals(AuctionStatus.OPEN, futureAuction.getStatus());
        assertThrows(AuctionNotStartedException.class, () -> futureAuction.processBid(bidder1, 1500));
    }

    // startTime <= Thời gian <= emdTime
    @ParameterizedTest
    @ValueSource(longs = {0, 1, 60, 3600})
    public void testProcessBidDuringAuction(long amount) {
        LocalDateTime nowStart = LocalDateTime.now().minusSeconds(amount);
        LocalDateTime nowEnd = LocalDateTime.now().plusSeconds(amount + 3600);
        Auction nowAuction = new Auction(item, seller, nowStart, nowEnd);

        assertEquals(AuctionStatus.RUNNING, nowAuction.getStatus());
        assertThrows(AuctionNotStartedException.class, () -> nowAuction.processBid(bidder1, 1500));
    }

    // Thời gian > endTime (đã kết thúc)
    @ParameterizedTest
    @ValueSource(longs = {1, 60, 3600})
    public void testProcessBidAfterAuctionEnd(long amount) {
        LocalDateTime pastStart = LocalDateTime.now().minusSeconds(amount + 3600);
        LocalDateTime pastEnd = LocalDateTime.now().minusSeconds(amount);
        Auction pastAuction = new Auction(item, seller, pastStart, pastEnd);
        pastAuction.startAuction();

        assertThrows(AuctionClosedException.class, () -> pastAuction.processBid(bidder1, 1500));
    }

    // Test 2 bidders đặt giá cùng lúc với giá KHÁC nhau
    @Test
    public void testConcurrentBidWithDifferentAmountsFirstLarger() throws InterruptedException {
        Thread thread1 = new Thread(() -> auction.processBid(bidder1, 2000.0));
        Thread thread2 = new Thread(() -> auction.processBid(bidder2, 3000.0));

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        assertEquals(bidder2, auction.getHighestBidder());
        assertEquals(3000.0, auction.getCurrentHighestBid(), 0.01);
    }

    // Test 2 bidders đặt CÙNG MỘT GIÁ cùng lúc
    @Test
    public void testConcurrentBidWithSameBidAmount() throws InterruptedException {
        // Arrange
        Bidder bidder3 = new Bidder("bidder3", "password", "Người mua 3", "Đà Nẵng");
        bidder3.deposit(15000);

        final double sameBidAmount = 2000.0;
        final boolean[] results = {false, false};
        final Exception[] exceptions = {null, null};

        // 2 threads đặt giá BẰNG NHAU
        Thread thread1 = new Thread(() -> {
            try {
                results[0] = auction.processBid(bidder1, sameBidAmount);
                System.out.println("Thread1 (bidder1): Success, highest bidder is " + auction.getHighestBidder().getUsername());
            } catch (Exception e) {
                exceptions[0] = e;
                System.out.println("Thread1 error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                results[1] = auction.processBid(bidder3, sameBidAmount);
                System.out.println("Thread2 (bidder3): Success, highest bidder is " + auction.getHighestBidder().getUsername());
            } catch (Exception e) {
                exceptions[1] = e;
                System.out.println("Thread2 error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
        });

        // Act: Chạy 2 threads cùng lúc
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Assert: Một người thành công, một người fail
        // Người đặt trước thành công, người sau throw BidTooLowException
        Bidder expectedWinner = (results[0]) ? bidder1 : bidder3;
        Exception expectedError = (exceptions[1] != null) ? exceptions[1] : exceptions[0];

        assertEquals(expectedWinner, auction.getHighestBidder(),
                "Concurrent (same bid): Người đặt trước phải thắng");
        assertEquals(sameBidAmount, auction.getCurrentHighestBid(), 0.01);

        // Người đặt sau phải nhận lỗi BidTooLowException
        assertNotNull(expectedError, "Người đặt sau phải throw exception");
        assertInstanceOf(BidTooLowException.class, expectedError);
    }

    // Balance của seller & bidder KHÔNG thay đổi cho tới khi closeAuction
    @Test
    public void testBalanceNotChangedDuringBidding() {
        double initialSellerBalance = seller.getBalance(); // 0
        double initialBidderBalance = bidder1.getBalance();

        auction.processBid(bidder1, 1500);

        assertEquals(initialSellerBalance, seller.getBalance(), 0.01);
        assertEquals(initialBidderBalance, bidder1.getBalance(), 0.01);
    }

    // closeAuction với winner
    @ParameterizedTest
    @ValueSource(doubles = {1500, 5000})
    public void testCloseAuctionWithWinner(double amount) {
        double initBidder1Balance = bidder1.getBalance(); // 5000
        double initSellerBalance = seller.getBalance();   // 0

        auction.processBid(bidder1, amount);
        auction.closeAuction();

        assertEquals(AuctionStatus.CANCELED, auction.getStatus());
        assertEquals(initBidder1Balance - amount, bidder1.getBalance(), 0.01);
        assertEquals(initSellerBalance + amount, seller.getBalance(), 0.01);
    }

    // Test closeAuction không có winner
    @Test
    public void testCloseAuctionWithNoWinner() {
        double initSellerBalance = seller.getBalance(); // 0

        auction.closeAuction();

        assertEquals(AuctionStatus.CANCELED, auction.getStatus());
        assertNull(auction.getHighestBidder());
        assertEquals(initSellerBalance, seller.getBalance(), 0.01);
    }

    // Test toàn bộ flow: multiple bids → closeAuction → verify results
    @Test
    public void testCompleteAuctionFlow() {
        // Initial state
        assertEquals(AuctionStatus.RUNNING, auction.getStatus());
        assertEquals(1000.0, auction.getCurrentHighestBid(), 0.01);

        // Bid 1: bidder1 đặt 1500$
        boolean bid1 = auction.processBid(bidder1, 1500.0);
        assertTrue(bid1);
        assertEquals(bidder1, auction.getHighestBidder());
        assertEquals(5000.0, bidder1.getBalance(), 0.01);

        // Bid 2: bidder2 đặt 3000$ (cao hơn)
        boolean bid2 = auction.processBid(bidder2, 3000.0);
        assertTrue(bid2);
        assertEquals(bidder2, auction.getHighestBidder());
        assertEquals(10000.0, bidder2.getBalance(), 0.01);

        // Close: Thanh toán
        auction.closeAuction();

        // Verify final state
        assertEquals(AuctionStatus.CANCELED, auction.getStatus());
        assertEquals(bidder2, auction.getHighestBidder());
        assertEquals(3000.0, auction.getCurrentHighestBid(), 0.01);

        // Verify payments
        assertEquals(7000.0, bidder2.getBalance(), 0.01);
        assertEquals(3000.0, seller.getBalance(), 0.01);
        assertEquals(5000.0, bidder1.getBalance(), 0.01);
    }
}