package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.model.AutoBidConfig;
import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.DTO.RegisterAutoBidRequest;
import com.auction.share.enums.Category;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoBidServiceTest {

    @Test
    void register_invalidMaxBid_throwsValidation() {
        FakeAuctionService auctionService = new FakeAuctionService();
        AutoBidService autoBidService = new AutoBidService(new AutoBidRegistry(), auctionService, sameThreadExecutor());

        RegisterAutoBidRequest req = new RegisterAutoBidRequest("a-1", 0, 10, "b-1");
        assertThrows(ValidationException.class, () -> autoBidService.register(req));
    }

    @Test
    void register_invalidIncrement_throwsValidation() {
        FakeAuctionService auctionService = new FakeAuctionService();
        AutoBidService autoBidService = new AutoBidService(new AutoBidRegistry(), auctionService, sameThreadExecutor());

        RegisterAutoBidRequest req = new RegisterAutoBidRequest("a-1", 200, 0, "b-1");
        assertThrows(ValidationException.class, () -> autoBidService.register(req));
    }

    @Test
    void triggerAutoBid_skipLastBidder() {
        FakeAuctionService auctionService = new FakeAuctionService();
        AutoBidRegistry registry = new AutoBidRegistry();
        AutoBidService autoBidService = new AutoBidService(registry, auctionService, sameThreadExecutor());

        registry.register(new AutoBidConfig("b-1", "a-1", 500, 50, LocalDateTime.now()));
        registry.register(new AutoBidConfig("b-2", "a-1", 450, 50, LocalDateTime.now().plusSeconds(1)));

        autoBidService.processAutoBid("a-1", "b-1");

        assertEquals(1, auctionService.placedBids.size());
        assertEquals("b-2", auctionService.placedBids.get(0).getBidderId());
    }

    @Test
    void triggerAutoBid_cancelWhenExceedMaxBid() {
        FakeAuctionService auctionService = new FakeAuctionService();
        auctionService.currentPrice = 100;
        AutoBidRegistry registry = new AutoBidRegistry();
        AutoBidService autoBidService = new AutoBidService(registry, auctionService, sameThreadExecutor());

        registry.register(new AutoBidConfig("b-2", "a-1", 120, 30, LocalDateTime.now()));
        autoBidService.processAutoBid("a-1", "b-1");

        assertTrue(registry.getConfigs("a-1").isEmpty());
        assertTrue(auctionService.placedBids.isEmpty());
    }

    @Test
    void triggerAutoBid_stopWhenAuctionClosed() {
        FakeAuctionService auctionService = new FakeAuctionService();
        auctionService.running = false;
        AutoBidRegistry registry = new AutoBidRegistry();
        AutoBidService autoBidService = new AutoBidService(registry, auctionService, sameThreadExecutor());

        registry.register(new AutoBidConfig("b-2", "a-1", 300, 20, LocalDateTime.now()));
        autoBidService.processAutoBid("a-1", "b-1");

        assertTrue(auctionService.placedBids.isEmpty());
    }

    private static ExecutorService sameThreadExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    private static class FakeAuctionService extends AuctionService {
        private final Auction auction;
        private final Map<String, Bidder> bidders = new HashMap<>();
        private final List<PlaceBidRequest> placedBids = new ArrayList<>();
        private boolean running = true;
        private double currentPrice = 100;

        private FakeAuctionService() {
            super(new AuctionDAO(), new ItemDAO(), new BidTransactionDAO(), new UserDAO(), new BidBroadcastService(null), null);
            Seller seller = new Seller("seller", "pwd", "Seller", "090", "s@mail.com", "HCM");
            seller.setID("s-1");
            Item item = new Item("Item", "Desc", 100, "s-1", Category.ITEM);
            this.auction = new Auction(item, seller, LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusMinutes(10));
            this.auction.setID("a-1");

            Bidder b1 = new Bidder("b1", "p", "Bidder 1", "090", "b1@mail.com", "HN");
            b1.setID("b-1");
            Bidder b2 = new Bidder("b2", "p", "Bidder 2", "090", "b2@mail.com", "HN");
            b2.setID("b-2");
            bidders.put("b-1", b1);
            bidders.put("b-2", b2);
        }

        @Override
        public Auction getAuctionById(String auctionId) {
            return auction;
        }

        @Override
        public boolean isAuctionRunning(Auction auction) {
            return running;
        }

        @Override
        public Bidder requireBidder(String bidderId) throws ValidationException {
            Bidder bidder = bidders.get(bidderId);
            if (bidder == null) {
                throw new ValidationException("User is not a bidder.");
            }
            return bidder;
        }

        @Override
        public boolean placeBidInternal(PlaceBidRequest req, boolean triggerAutoBid) throws SQLException, ValidationException {
            if (!running) {
                throw new ValidationException("Auction is not running.");
            }
            if (req.getAmount() <= currentPrice) {
                throw new ValidationException("Bid amount must be higher than current highest bid.");
            }
            currentPrice = req.getAmount();
            placedBids.add(req);
            return true;
        }
    }
}
