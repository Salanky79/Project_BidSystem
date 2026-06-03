package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.util.AutoBidConfig;
import com.auction.share.DTO.CancelAutoBidRequest;
import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.DTO.RegisterAutoBidRequest;
import com.auction.share.enums.Category;
import com.auction.share.exceptions.ConcurrentBidException;
import com.auction.share.exceptions.InsufficientBalanceException;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import javax.sql.DataSource;

@ExtendWith(MockitoExtension.class)
class AutoBidServiceTest {

    @Mock
    private AutoBidRegistry registry;
    @Mock
    private BidService bidService;
    @Mock
    private UserDAO userDAO;
    @Mock
    private AuctionDAO auctionDAO;
    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private BroadcastService broadcastService;

    private AutoBidService createService() throws Exception {
        lenient().when(dataSource.getConnection()).thenReturn(connection);
        return new AutoBidService(
                dataSource, registry, bidService, userDAO, auctionDAO, broadcastService
        );
    }



    @Test
    void processAutoBid_twoBidders_highestWinsAtProxyPrice() throws Exception {
        AutoBidService service = createService();
        Auction auction = runningAuction(100);
        auction.setID("a-1");

        when(auctionDAO.findById(any(), eq("a-1"))).thenReturn(auction);
        when(registry.getConfigs("a-1")).thenReturn(List.of(
                new AutoBidConfig("b-1", "a-1", 500, 50, LocalDateTime.now()),
                new AutoBidConfig("b-2", "a-1", 400, 50, LocalDateTime.now().plusSeconds(1))
        ));

        // Stub balance checks
        when(userDAO.findBalanceForUpdate(any(), eq("b-1"))).thenReturn(1000.0);
        when(userDAO.findBalanceForUpdate(any(), eq("b-2"))).thenReturn(1000.0);
        when(auctionDAO.sumAuctionCurrentPrices(any(), eq("b-1"), eq("a-1"))).thenReturn(0.0);
        when(auctionDAO.sumAuctionCurrentPrices(any(), eq("b-2"), eq("a-1"))).thenReturn(0.0);

        when(bidService.placeBid(any(PlaceBidRequest.class), anyBoolean())).thenReturn(true);

        service.processAutoBid("a-1");

        // Verify winner b-1 wins at proxy price: 400 + 50 = 450.0
        verify(bidService).placeBid(argThat(req -> 
                req.getAuctionId().equals("a-1") && 
                req.getBidderId().equals("b-1") && 
                Double.compare(req.getAmount(), 450.0) == 0
        ), eq(false));
    }

    @Test
    void processAutoBid_oneBidder_winsAtNextMinimumBid() throws Exception {
        AutoBidService service = createService();
        Auction auction = runningAuction(100);
        auction.setID("a-1");

        when(auctionDAO.findById(any(), eq("a-1"))).thenReturn(auction);
        when(registry.getConfigs("a-1")).thenReturn(List.of(
                new AutoBidConfig("b-1", "a-1", 500, 50, LocalDateTime.now())
        ));

        // Stub balance checks
        when(userDAO.findBalanceForUpdate(any(), eq("b-1"))).thenReturn(1000.0);
        when(auctionDAO.sumAuctionCurrentPrices(any(), eq("b-1"), eq("a-1"))).thenReturn(0.0);

        when(bidService.placeBid(any(PlaceBidRequest.class), anyBoolean())).thenReturn(true);

        service.processAutoBid("a-1");

        // Verify winner b-1 wins at minimum next bid: 100 + 50 = 150.0
        verify(bidService).placeBid(argThat(req -> 
                req.getAuctionId().equals("a-1") && 
                req.getBidderId().equals("b-1") && 
                Double.compare(req.getAmount(), 150.0) == 0
        ), eq(false));
    }

    @Test
    void processAutoBid_oneBidderAlreadyLeading_noOp() throws Exception {
        AutoBidService service = createService();
        Auction auction = runningAuction(100);
        auction.setID("a-1");
        
        // Make b-1 already the highest bidder in DB
        Bidder leader = new Bidder("b", "p", "Leader", "090", "l@mail.com", "HN");
        leader.setID("b-1");
        auction.setHighestBid(leader, 100);

        when(auctionDAO.findById(any(), eq("a-1"))).thenReturn(auction);
        when(registry.getConfigs("a-1")).thenReturn(List.of(
                new AutoBidConfig("b-1", "a-1", 500, 50, LocalDateTime.now())
        ));

        service.processAutoBid("a-1");

        // Verify no bids are placed because they are already leading
        verify(bidService, never()).placeBid(any(PlaceBidRequest.class), anyBoolean());
    }

    // ── register() validation ──────────────────────────────────────





    // ── processAutoBid() edge cases ────────────────────────────────



    @Test
    void processAutoBid_concurrentBid_retriesAndSucceeds() throws Exception {
        AutoBidService service = createService();
        Auction auction = runningAuction(100);

        when(auctionDAO.findById(any(), eq("a-1"))).thenReturn(auction);
        when(registry.getConfigs("a-1")).thenReturn(List.of(
                new AutoBidConfig("b-1", "a-1", 500, 50, LocalDateTime.now())
        ));
        when(userDAO.findBalanceForUpdate(any(), eq("b-1"))).thenReturn(1000.0);
        when(auctionDAO.sumAuctionCurrentPrices(any(), eq("b-1"), eq("a-1"))).thenReturn(0.0);

        // First call throws ConcurrentBidException, second call succeeds
        when(bidService.placeBid(any(PlaceBidRequest.class), anyBoolean()))
                .thenThrow(new ConcurrentBidException("Price changed."))
                .thenReturn(true);

        service.processAutoBid("a-1");

        // placeBid must have been called exactly twice (retry)
        verify(bidService, times(2)).placeBid(any(PlaceBidRequest.class), anyBoolean());
    }

    @Test
    void cancel_removesFromRegistry() throws Exception {
        AutoBidService service = createService();
        when(registry.cancel("a-1", "b-1")).thenReturn(true);

        CancelAutoBidRequest req = new CancelAutoBidRequest("a-1", "b-1");
        req.withUserId("b-1");
        boolean result = service.cancel(req);

        verify(registry).cancel("a-1", "b-1");
        assert result;
    }

    // ── helpers ────────────────────────────────────────────────────

    private static Auction finishedAuction() {
        Seller seller = new Seller("seller", "pwd", "Seller", "090", "s@mail.com", "HCM");
        seller.setID("s-1");
        Item item = new Item("Item", "Desc", 100, seller.getId(), Category.ITEM);
        // end time in the past → isRunning() returns false
        return new Auction(item, seller, LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1));
    }

    private static Auction runningAuction(double currentPrice) {
        Seller seller = new Seller("seller", "pwd", "Seller", "090", "s@mail.com", "HCM");
        seller.setID("s-1");
        Item item = new Item("Item", "Desc", 100, seller.getId(), Category.ITEM);
        Auction auction = new Auction(item, seller, LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusMinutes(30));
        auction.setID("a-1");
        auction.markRunning();
        
        Bidder bidder = new Bidder("seed", "pwd", "Seed", "090", "seed@mail.com", "HN");
        bidder.setID("seed-1");
        auction.setHighestBid(bidder, currentPrice);
        
        return auction;
    }
}
