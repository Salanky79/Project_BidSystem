package com.auction.share.models.auction;

import com.auction.share.enums.AuctionStatus;
import com.auction.share.enums.Category;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Seller;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionTest {

    @Test
    void isRunning_whenStatusRunningAndWithinTime_returnsTrue() {
        Item item = new Item("Laptop", "Desc", 1000, "s-1", Category.ELECTRONIC);
        Seller seller = new Seller("seller", "pass", "Seller", "01", "a@a", "Addr");
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        
        Auction auction = new Auction(item, seller, start, end);
        auction.markRunning();

        assertTrue(auction.isRunning());
    }

    @Test
    void isRunning_whenStatusNotRunning_returnsFalse() {
        Item item = new Item("Laptop", "Desc", 1000, "s-1", Category.ELECTRONIC);
        Seller seller = new Seller("seller", "pass", "Seller", "01", "a@a", "Addr");
        // startTime in the future → constructor sets OPEN status
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        
        Auction auction = new Auction(item, seller, start, end);
        
        // Status is OPEN (not RUNNING), so isRunning() must return false
        assertFalse(auction.isRunning());
    }

    @Test
    void isRunning_whenTimeExpired_returnsFalse() {
        Item item = new Item("Laptop", "Desc", 1000, "s-1", Category.ELECTRONIC);
        Seller seller = new Seller("seller", "pass", "Seller", "01", "a@a", "Addr");
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        
        Auction auction = new Auction(item, seller, start, end);
        auction.markRunning();

        assertFalse(auction.isRunning());
    }
}
