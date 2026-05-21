package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.enums.Category;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionServicePlaceBidTest {

    @Test
    void placeBidShouldThrowWhenAuctionNotFound() {
        AuctionService service = new AuctionService(
                new FakeAuctionDAO(null),
                new ItemDAO(),
                new BidTransactionDAO(),
                new FakeUserDAO(null),
                null
        );

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.placeBid(new PlaceBidRequest("a1", "b1", 100)));
        assertEquals("Auction not found.", ex.getMessage());
    }

    @Test
    void placeBidShouldThrowWhenAuctionNotRunningByTime() {
        Auction endedAuction = buildAuction(LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1), 100);
        Bidder bidder = new Bidder("bidder", "pwd", "Bidder", "090", "b@mail.com", "HN");
        bidder.setID("bidder-1");

        AuctionService service = new AuctionService(
                new FakeAuctionDAO(endedAuction),
                new ItemDAO(),
                new BidTransactionDAO(),
                new FakeUserDAO(bidder),
                null
        );

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.placeBid(new PlaceBidRequest(endedAuction.getId(), bidder.getId(), 120)));
        assertEquals("Auction is not running.", ex.getMessage());
    }

    @Test
    void placeBidShouldThrowWhenUserIsNotBidder() {
        Auction runningAuction = buildAuction(LocalDateTime.now().minusMinutes(5), LocalDateTime.now().plusMinutes(30), 100);
        Seller sellerUser = new Seller("seller", "pwd", "Seller", "091", "s@mail.com", "HCM");
        sellerUser.setID("seller-1");

        AuctionService service = new AuctionService(
                new FakeAuctionDAO(runningAuction),
                new ItemDAO(),
                new BidTransactionDAO(),
                new FakeUserDAO(sellerUser),
                null
        );

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.placeBid(new PlaceBidRequest(runningAuction.getId(), sellerUser.getId(), 130)));
        assertEquals("User is not a bidder.", ex.getMessage());
    }

    @Test
    void placeBidShouldThrowWhenAmountNotHigherThanCurrent() {
        Auction runningAuction = buildAuction(LocalDateTime.now().minusMinutes(5), LocalDateTime.now().plusMinutes(30), 100);
        Bidder bidder = new Bidder("bidder", "pwd", "Bidder", "090", "b@mail.com", "HN");
        bidder.setID("bidder-1");

        AuctionService service = new AuctionService(
                new FakeAuctionDAO(runningAuction),
                new ItemDAO(),
                new BidTransactionDAO(),
                new FakeUserDAO(bidder),
                null
        );

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.placeBid(new PlaceBidRequest(runningAuction.getId(), bidder.getId(), 100)));
        assertEquals("Bid amount must be higher than current highest bid.", ex.getMessage());
    }

    private static Auction buildAuction(LocalDateTime start, LocalDateTime end, double currentHighest) {
        Seller seller = new Seller("seller", "pwd", "Seller", "091", "s@mail.com", "HCM");
        seller.setID("seller-1");
        Item item = new Item("Laptop", "Good", 100, seller.getId(), Category.ELECTRONIC);
        Auction auction = new Auction(item, seller, start, end);
        if (currentHighest > item.getStartingPrice()) {
            Bidder existing = new Bidder("existing", "pwd", "Existing", "099", "e@mail.com", "HN");
            existing.setID("existing-1");
            auction.setHighestBid(existing, currentHighest);
        }
        return auction;
    }

    private static class FakeAuctionDAO extends AuctionDAO {
        private final Auction auction;

        private FakeAuctionDAO(Auction auction) {
            this.auction = auction;
        }

        @Override
        public Auction findById(String id) {
            return auction;
        }

        @Override
        public boolean updateHighestBidIfHigher(Connection conn, String id, String bidderId, double amount) throws SQLException {
            throw new UnsupportedOperationException();
        }
    }

    private static class FakeUserDAO extends UserDAO {
        private final User user;

        private FakeUserDAO(User user) {
            this.user = user;
        }

        @Override
        public User findById(String id) {
            return user;
        }
    }
}

