package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.share.DTO.CreateAuctionRequest;
import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.enums.Category;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionDAO auctionDAO;
    @Mock
    private ItemDAO itemDAO;
    @Mock
    private BidTransactionDAO bidTransactionDAO;
    @Mock
    private UserDAO userDAO;
    @Mock
    private BidBroadcastService bidBroadcastService;
    @Mock
    private AutoBidService autoBidService;

    @Test
    void createAuction_startAfterEnd_throwsValidation() throws Exception {
        Seller seller = seller("seller-1");
        when(userDAO.findById("seller-1")).thenReturn(seller);

        AuctionService service = new AuctionService(auctionDAO, itemDAO, bidTransactionDAO, userDAO, bidBroadcastService, autoBidService);
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        CreateAuctionRequest request = createRequest("seller-1", start, end);

        assertThrows(ValidationException.class, () -> service.createAuction(request));
    }

    @Test
    void createAuction_userNotSeller_throwsValidation() throws Exception {
        Bidder bidder = new Bidder("b", "p", "Bidder", "090", "b@mail.com", "HN");
        when(userDAO.findById("seller-1")).thenReturn(bidder);

        AuctionService service = new AuctionService(auctionDAO, itemDAO, bidTransactionDAO, userDAO, bidBroadcastService, autoBidService);
        CreateAuctionRequest request = createRequest("seller-1", LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        assertThrows(ValidationException.class, () -> service.createAuction(request));
    }

    @Test
    void createAuction_success_savesItemAndAuction() throws Exception {
        Seller seller = seller("seller-1");
        when(userDAO.findById("seller-1")).thenReturn(seller);
        when(itemDAO.saveItem(any(Item.class))).thenReturn(true);
        when(auctionDAO.saveAuction(any(Auction.class))).thenReturn(true);

        AuctionService service = new AuctionService(auctionDAO, itemDAO, bidTransactionDAO, userDAO, bidBroadcastService, autoBidService);
        CreateAuctionRequest request = createRequest("seller-1", LocalDateTime.now().plusMinutes(1), LocalDateTime.now().plusHours(2));

        service.createAuction(request);

        verify(itemDAO).saveItem(any(Item.class));
        verify(auctionDAO).saveAuction(any(Auction.class));
    }

    @Test
    void placeBid_auctionNotFound_throwsValidation() throws Exception {
        when(auctionDAO.findById("a-1")).thenReturn(null);
        AuctionService service = testableService();

        assertThrows(ValidationException.class, () -> service.placeBid(new PlaceBidRequest("a-1", "b-1", 200)));
    }

    @Test
    void placeBid_auctionNotRunning_throwsValidation() throws Exception {
        Seller seller = seller("s-1");
        Auction auction = new Auction(
                new Item("Phone", "Desc", 100, "s-1", Category.ITEM),
                seller,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1)
        );
        when(auctionDAO.findById("a-1")).thenReturn(auction);
        AuctionService service = testableService();

        assertThrows(ValidationException.class, () -> service.placeBid(new PlaceBidRequest("a-1", "b-1", 200)));
    }

    @Test
    void placeBid_amountTooLow_throwsValidation() throws Exception {
        Auction auction = runningAuction(100);
        Bidder bidder = new Bidder("b", "p", "Bidder", "090", "b@mail.com", "HN");
        bidder.setID("b-1");
        when(auctionDAO.findById("a-1")).thenReturn(auction);
        when(userDAO.findById("b-1")).thenReturn(bidder);
        AuctionService service = testableService();

        assertThrows(ValidationException.class, () -> service.placeBid(new PlaceBidRequest("a-1", "b-1", 100)));
    }

    @Test
    void placeBid_userNotBidder_throwsValidation() throws Exception {
        Auction auction = runningAuction(100);
        Seller seller = seller("s-2");
        when(auctionDAO.findById("a-1")).thenReturn(auction);
        when(userDAO.findById("x-1")).thenReturn(seller);
        AuctionService service = testableService();

        assertThrows(ValidationException.class, () -> service.placeBid(new PlaceBidRequest("a-1", "x-1", 150)));
    }

    @Test
    void placeBid_success_returnsTrue() throws Exception {
        Auction auction = runningAuction(100);
        Bidder bidder = new Bidder("b", "p", "Bidder", "090", "b@mail.com", "HN");
        bidder.setID("b-1");
        when(auctionDAO.findById("a-1")).thenReturn(auction);
        when(userDAO.findById("b-1")).thenReturn(bidder);
        when(auctionDAO.updateHighestBidIfHigher(any(Connection.class), eq(auction.getId()), eq("b-1"), eq(150.0))).thenReturn(true);
        when(bidTransactionDAO.saveBidTransaction(any(Connection.class), any())).thenReturn(true);

        AuctionService service = testableService();
        boolean ok = service.placeBid(new PlaceBidRequest("a-1", "b-1", 150));

        assertTrue(ok);
        verify(auctionDAO).updateHighestBidIfHigher(any(Connection.class), eq(auction.getId()), eq("b-1"), eq(150.0));
    }

    private AuctionService testableService() {
        return new AuctionService(auctionDAO, itemDAO, bidTransactionDAO, userDAO, bidBroadcastService, autoBidService) {
            @Override
            protected Connection getConnection() {
                return fakeConnection();
            }
        };
    }

    private static Connection fakeConnection() {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("setAutoCommit".equals(name) || "commit".equals(name) || "rollback".equals(name) || "close".equals(name)) {
                        return null;
                    }
                    if ("isClosed".equals(name)) {
                        return false;
                    }
                    throw new UnsupportedOperationException("Unsupported Connection method in test: " + name);
                }
        );
    }

    private static CreateAuctionRequest createRequest(String sellerId, LocalDateTime start, LocalDateTime end) {
        return new CreateAuctionRequest(
                sellerId,
                "Phone",
                "Desc",
                "item",
                100,
                start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                false
        );
    }

    private static Seller seller(String id) {
        Seller seller = new Seller("seller", "pass", "Seller", "090", "s@mail.com", "HCM");
        seller.setID(id);
        return seller;
    }

    private static Auction runningAuction(double startingPrice) {
        Seller seller = seller("s-1");
        Item item = new Item("Item", "Desc", startingPrice, seller.getId(), Category.ITEM);
        Auction auction = new Auction(item, seller, LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusMinutes(20));
        auction.setID("auction-1");
        return auction;
    }
}

