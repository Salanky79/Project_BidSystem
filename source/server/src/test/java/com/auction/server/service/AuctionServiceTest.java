package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.util.ImageStorage;
import com.auction.share.DTO.CreateAuctionRequest;
import com.auction.share.DTO.SetBidStepRequest;
import com.auction.share.enums.AuctionStatus;
import com.auction.share.enums.Category;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock private AuctionDAO auctionDAO;
    @Mock private ItemDAO itemDAO;
    @Mock private UserDAO userDAO;
    @Mock private ImageStorage imageStorage;
    @Mock private DataSource dataSource;

    private Connection mockConn;

    @BeforeEach
    void setUp() throws Exception {
        mockConn = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(mockConn);
    }

    // ── createAuction ─────────────────────────────────────────────

    @Test
    void createAuction_startAfterEnd_throwsValidation() throws Exception {
        Seller seller = seller("seller-1");
        when(userDAO.findById(any(Connection.class), eq("seller-1"))).thenReturn(seller);

        AuctionService service = new AuctionService(dataSource, auctionDAO, itemDAO, userDAO, imageStorage);
        CreateAuctionRequest request = createRequest("seller-1",
                LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(1));

        assertThrows(ValidationException.class, () -> service.createAuction(request));
    }

    @Test
    void createAuction_userNotSeller_throwsValidation() throws Exception {
        Bidder bidder = new Bidder("b", "p", "Bidder", "090", "b@mail.com", "HN");
        when(userDAO.findById(any(Connection.class), eq("seller-1"))).thenReturn(bidder);

        AuctionService service = new AuctionService(dataSource, auctionDAO, itemDAO, userDAO, imageStorage);
        CreateAuctionRequest request = createRequest("seller-1",
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        assertThrows(ValidationException.class, () -> service.createAuction(request));
    }

    @Test
    void createAuction_success_savesItemAndAuction() throws Exception {
        Seller seller = seller("seller-1");
        when(userDAO.findById(any(Connection.class), eq("seller-1"))).thenReturn(seller);
        when(itemDAO.saveItem(any(Connection.class), any(Item.class))).thenReturn(true);
        when(auctionDAO.saveAuction(any(Connection.class), any(Auction.class))).thenReturn(true);

        AuctionService service = new AuctionService(dataSource, auctionDAO, itemDAO, userDAO, imageStorage);
        CreateAuctionRequest request = createRequest("seller-1",
                LocalDateTime.now().plusMinutes(1), LocalDateTime.now().plusHours(2));

        service.createAuction(request);

        verify(itemDAO).saveItem(any(Connection.class), any(Item.class));
        verify(auctionDAO).saveAuction(any(Connection.class), any(Auction.class));
    }

    // ── cancelAuction ─────────────────────────────────────────────

    @Test
    void cancelAuction_auctionNotFound_throwsValidation() throws Exception {
        when(auctionDAO.findById(any(Connection.class), eq("a-99"))).thenReturn(null);

        AuctionService service = new AuctionService(dataSource, auctionDAO, itemDAO, userDAO, imageStorage);
        assertThrows(ValidationException.class, () -> service.cancelAuction("a-99", "seller-1"));
    }

    @Test
    void cancelAuction_wrongSeller_throwsValidation() throws Exception {
        Auction auction = runningAuction(100);
        when(auctionDAO.findById(any(Connection.class), eq("auction-1"))).thenReturn(auction);

        AuctionService service = new AuctionService(dataSource, auctionDAO, itemDAO, userDAO, imageStorage);
        assertThrows(ValidationException.class,
                () -> service.cancelAuction("auction-1", "wrong-seller"));
    }

    @Test
    void cancelAuction_success_updatesStatus() throws Exception {
        Auction auction = runningAuction(100);
        when(auctionDAO.findById(any(Connection.class), eq("auction-1"))).thenReturn(auction);
        when(auctionDAO.updateStatus(any(), eq("auction-1"), eq(AuctionStatus.CANCELED))).thenReturn(true);

        AuctionService service = new AuctionService(dataSource, auctionDAO, itemDAO, userDAO, imageStorage);
        service.cancelAuction("auction-1", "s-1");

        verify(auctionDAO).updateStatus(any(), eq("auction-1"), eq(AuctionStatus.CANCELED));
    }

    // ── setBidStep ────────────────────────────────────────────────

    @Test
    void setBidStep_auctionNotFound_throwsValidation() throws Exception {
        when(auctionDAO.findById(any(Connection.class), eq("a-99"))).thenReturn(null);

        AuctionService service = new AuctionService(dataSource, auctionDAO, itemDAO, userDAO, imageStorage);
        SetBidStepRequest req = (SetBidStepRequest) new SetBidStepRequest("a-99", 50.0, "s-1").withUserId("s-1");
        assertThrows(ValidationException.class, () -> service.setBidStep(req));
    }

    @Test
    void setBidStep_notSeller_throwsValidation() throws Exception {
        Auction auction = runningAuction(100);
        when(auctionDAO.findById(any(Connection.class), eq("auction-1"))).thenReturn(auction);

        AuctionService service = new AuctionService(dataSource, auctionDAO, itemDAO, userDAO, imageStorage);
        SetBidStepRequest req = (SetBidStepRequest) new SetBidStepRequest("auction-1", 50.0, "wrong-seller").withUserId("wrong-seller");
        assertThrows(ValidationException.class, () -> service.setBidStep(req));
    }

    @Test
    void setBidStep_zeroBidStep_throwsValidation() throws Exception {
        Auction auction = runningAuction(100);
        when(auctionDAO.findById(any(Connection.class), eq("auction-1"))).thenReturn(auction);

        AuctionService service = new AuctionService(dataSource, auctionDAO, itemDAO, userDAO, imageStorage);
        SetBidStepRequest req = (SetBidStepRequest) new SetBidStepRequest("auction-1", 0.0, "s-1").withUserId("s-1");
        assertThrows(ValidationException.class, () -> service.setBidStep(req));
    }

    @Test
    void setBidStep_success_updatesDAO() throws Exception {
        Auction auction = runningAuction(100);
        when(auctionDAO.findById(any(Connection.class), eq("auction-1"))).thenReturn(auction);
        when(auctionDAO.updateBidStep(any(Connection.class), eq("auction-1"), eq(75.0))).thenReturn(true);

        AuctionService service = new AuctionService(dataSource, auctionDAO, itemDAO, userDAO, imageStorage);
        SetBidStepRequest req = (SetBidStepRequest) new SetBidStepRequest("auction-1", 75.0, "s-1").withUserId("s-1");
        boolean result = service.setBidStep(req);

        assertTrue(result);
        verify(auctionDAO).updateBidStep(any(Connection.class), eq("auction-1"), eq(75.0));
    }

    // ── helpers ───────────────────────────────────────────────────

    private static CreateAuctionRequest createRequest(String sellerId, LocalDateTime start, LocalDateTime end) {
        return (CreateAuctionRequest) new CreateAuctionRequest(
                sellerId, "Phone", "Desc", "item", 100.0,
                start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        ).withUserId(sellerId);
    }

    private static Seller seller(String id) {
        Seller seller = new Seller("seller", "pass", "Seller", "090", "s@mail.com", "HCM");
        seller.setID(id);
        return seller;
    }

    private static Auction runningAuction(double startingPrice) {
        Seller seller = seller("s-1");
        Item item = new Item("Item", "Desc", startingPrice, seller.getId(), Category.ITEM);
        Auction auction = new Auction(item, seller,
                LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusMinutes(20));
        auction.setID("auction-1");
        auction.markRunning();
        return auction;
    }
}
