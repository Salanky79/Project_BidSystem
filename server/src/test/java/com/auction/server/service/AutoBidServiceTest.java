package com.auction.server.service;

import com.auction.server.util.AutoBidConfig;
import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.DTO.RegisterAutoBidRequest;
import com.auction.share.enums.Category;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.auction.server.dao.UserDAO;
import javax.sql.DataSource;

@ExtendWith(MockitoExtension.class)
class AutoBidServiceTest {

    @Mock
    private AutoBidRegistry registry;

    @Mock
    private BidService bidService;
    @Mock
    private AuctionQueryService auctionQueryService;
    @Mock
    private UserDAO userDAO;
    @Mock
    private DataSource dataSource;

    @Test
    void register_invalidMaxBid_throwsValidation() {
        AutoBidService service = new AutoBidService(dataSource, registry, bidService, auctionQueryService, userDAO);

        RegisterAutoBidRequest req = new RegisterAutoBidRequest("a-1", 0, 10, "b-1");
        assertThrows(ValidationException.class, () -> service.register(req));
        verify(registry, never()).register(any());
    }

    @Test
    void register_invalidIncrement_throwsValidation() {
        AutoBidService service = new AutoBidService(dataSource, registry, bidService, auctionQueryService, userDAO);

        RegisterAutoBidRequest req = new RegisterAutoBidRequest("a-1", 200, 0, "b-1");
        assertThrows(ValidationException.class, () -> service.register(req));
        verify(registry, never()).register(any());
    }

    @Test
    void triggerAutoBid_skipLastBidder() throws Exception {
        AutoBidService service = new AutoBidService(dataSource, registry, bidService, auctionQueryService, userDAO);
        Auction auction = org.mockito.Mockito.spy(runningAuction(100));

        when(auctionQueryService.getAuctionById("a-1")).thenReturn(auction);
        when(auction.isRunning()).thenReturn(true, false);
        when(registry.getConfigs("a-1")).thenReturn(List.of(
                new AutoBidConfig("b-1", "a-1", 500, 50, LocalDateTime.now()),
                new AutoBidConfig("b-2", "a-1", 450, 50, LocalDateTime.now().plusSeconds(1))
        ));

        doAnswer(invocation -> {
            PlaceBidRequest req = invocation.getArgument(0);
            Bidder bidder = new Bidder("b", "p", "Bidder", "090", "b@mail.com", "HN");
            bidder.setID(req.getBidderId());
            auction.setHighestBid(bidder, req.getAmount());
            return true;
        }).when(bidService).placeBid(any(PlaceBidRequest.class));

        service.processAutoBid("a-1", "b-1");

        ArgumentCaptor<PlaceBidRequest> captor = ArgumentCaptor.forClass(PlaceBidRequest.class);
        verify(bidService, times(1)).placeBid(captor.capture());
        assertEquals("b-2", captor.getValue().getBidderId());
        assertEquals(150.0, captor.getValue().getAmount(), 0.001);
    }

    @Test
    void triggerAutoBid_cancelWhenExceedMaxBid() throws Exception {
        AutoBidService service = new AutoBidService(dataSource, registry, bidService, auctionQueryService, userDAO);
        Auction auction = org.mockito.Mockito.spy(runningAuction(100));

        when(auctionQueryService.getAuctionById("a-1")).thenReturn(auction);
        when(auction.isRunning()).thenReturn(true, true, false);
        when(registry.getConfigs("a-1")).thenReturn(
                List.of(new AutoBidConfig("b-2", "a-1", 120, 30, LocalDateTime.now())),
                List.of()
        );

        service.processAutoBid("a-1", "b-1");

        verify(registry).cancel("a-1", "b-2");
        verify(bidService, never()).placeBid(any(PlaceBidRequest.class));
    }

    @Test
    void triggerAutoBid_stopWhenAuctionClosed() throws Exception {
        AutoBidService service = new AutoBidService(dataSource, registry, bidService, auctionQueryService, userDAO);
        Auction auction = org.mockito.Mockito.spy(runningAuction(100));

        when(auctionQueryService.getAuctionById("a-1")).thenReturn(auction);
        when(auction.isRunning()).thenReturn(false);

        service.processAutoBid("a-1", "b-1");

        verify(registry, never()).getConfigs(any());
        verify(bidService, never()).placeBid(any(PlaceBidRequest.class));
    }

    private static Auction runningAuction(double currentPrice) {
        Seller seller = new Seller("seller", "pwd", "Seller", "090", "s@mail.com", "HCM");
        seller.setID("s-1");
        Item item = new Item("Item", "Desc", 100, seller.getId(), Category.ITEM);
        Auction auction = new Auction(item, seller, LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusMinutes(30));
        auction.setID("a-1");
        if (currentPrice > auction.getCurrentHighestBid()) {
            Bidder bidder = new Bidder("seed", "pwd", "Seed", "090", "seed@mail.com", "HN");
            bidder.setID("seed-1");
            auction.setHighestBid(bidder, currentPrice);
        }
        return auction;
    }
}
