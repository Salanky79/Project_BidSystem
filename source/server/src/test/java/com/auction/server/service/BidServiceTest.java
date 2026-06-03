package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.enums.Category;
import com.auction.share.exceptions.ConcurrentBidException;
import com.auction.share.exceptions.InsufficientBalanceException;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock private DataSource dataSource;
    @Mock private AuctionDAO auctionDAO;
    @Mock private BidTransactionDAO bidTransactionDAO;
    @Mock private UserDAO userDAO;
    @Mock private BroadcastService broadcastService;
    @Mock private AutoBidService autoBidService;
    @Mock private Connection connection;

    private BidService bidService;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(dataSource.getConnection()).thenReturn(connection);
        bidService = new BidService(dataSource, auctionDAO, bidTransactionDAO, userDAO, broadcastService);
        bidService.setAutoBidService(autoBidService);
    }

    @Test
    void placeBid_invalidBidAmount_throwsConcurrentBidException() throws Exception {
        Auction auction = createRunningAuction("a-1", 100, 10);
        when(auctionDAO.findById(connection, "a-1")).thenReturn(auction);
        
        Bidder bidder = createBidder("b-1");
        when(userDAO.findById(connection, "b-1")).thenReturn(bidder);

        PlaceBidRequest request = (PlaceBidRequest) new PlaceBidRequest("a-1", "b-1", 105).withUserId("b-1");
        
        assertThrows(ConcurrentBidException.class, () -> bidService.placeBid(request, false),
                "Should throw exception because amount 105 is less than current price (100) + step (10)");
    }

    @Test
    void placeBid_insufficientBalance_throwsException() throws Exception {
        Auction auction = createRunningAuction("a-1", 100, 10);
        when(auctionDAO.findById(connection, "a-1")).thenReturn(auction);
        
        Bidder bidder = createBidder("b-1");
        when(userDAO.findById(connection, "b-1")).thenReturn(bidder);
        
        // Mock user balance 50, but trying to bid 150
        when(userDAO.findBalanceForUpdate(connection, "b-1")).thenReturn(50.0);
        when(auctionDAO.sumAuctionCurrentPrices(connection, "b-1", "a-1")).thenReturn(0.0);

        PlaceBidRequest request = (PlaceBidRequest) new PlaceBidRequest("a-1", "b-1", 150).withUserId("b-1");
        
        assertThrows(InsufficientBalanceException.class, () -> bidService.placeBid(request, false));
    }

    @Test
    void placeBid_success_deductsMoneyAndBroadcasts() throws Exception {
        Auction auction = createRunningAuction("a-1", 100, 10);
        when(auctionDAO.findById(connection, "a-1")).thenReturn(auction);
        
        Bidder bidder = createBidder("b-1");
        when(userDAO.findById(connection, "b-1")).thenReturn(bidder);
        
        // Mock enough balance
        when(userDAO.findBalanceForUpdate(connection, "b-1")).thenReturn(1000.0);
        when(auctionDAO.sumAuctionCurrentPrices(connection, "b-1", "a-1")).thenReturn(0.0);
        
        when(auctionDAO.updateHighestBid(eq(connection), eq("a-1"), eq("b-1"), eq(150.0), any(LocalDateTime.class))).thenReturn(true);

        PlaceBidRequest request = (PlaceBidRequest) new PlaceBidRequest("a-1", "b-1", 150).withUserId("b-1");
        
        boolean result = bidService.placeBid(request, true);
        
        assertTrue(result);
        
        verify(bidTransactionDAO).saveBidTransaction(eq(connection), any());
        verify(connection).commit();
        
        // Verify broadcast
        ArgumentCaptor<BidUpdateEvent> eventCaptor = ArgumentCaptor.forClass(BidUpdateEvent.class);
        verify(broadcastService).broadcastBidUpdate(eventCaptor.capture());
        assertEquals(150.0, eventCaptor.getValue().getCurrentHighestBid());
        
        // Verify auto bid triggered
        verify(autoBidService).triggerAutoBid("a-1", "b-1");
    }

    private Auction createRunningAuction(String id, double currentPrice, double step) {
        Seller seller = new Seller("s", "p", "Seller", "0", "s@mail.com", "Addr");
        seller.setID("s-1");
        Item item = new Item("I", "D", 100, "s-1", Category.ITEM);
        Auction a = new Auction(item, seller, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));
        a.setID(id);
        a.markRunning();
        Bidder seed = createBidder("seed");
        a.setHighestBid(seed, currentPrice);
        a.setBidStep(step);
        return a;
    }
    
    private Bidder createBidder(String id) {
        Bidder b = new Bidder("b", "p", "Bidder", "0", "b@mail.com", "Addr");
        b.setID(id);
        return b;
    }
}
