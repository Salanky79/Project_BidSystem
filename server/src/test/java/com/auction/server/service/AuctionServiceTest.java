package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.util.ImageStorage;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.mockito.Mockito.mock;
import javax.sql.DataSource;
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
    private UserDAO userDAO;
    @Mock
    private ImageStorage imageStorage;

    @Mock
    private DataSource dataSource;
    private Connection mockConn;

    @BeforeEach
    void setUp() throws Exception {
        mockConn = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(mockConn);
    }

    @Test
    void createAuction_startAfterEnd_throwsValidation() throws Exception {
        Seller seller = seller("seller-1");
        when(userDAO.findById(any(Connection.class), eq("seller-1"))).thenReturn(seller);

        AuctionService service = new AuctionService(dataSource, auctionDAO, itemDAO, userDAO, imageStorage);
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        CreateAuctionRequest request = createRequest("seller-1", start, end);

        assertThrows(ValidationException.class, () -> service.createAuction(request));
    }

    @Test
    void createAuction_userNotSeller_throwsValidation() throws Exception {
        Bidder bidder = new Bidder("b", "p", "Bidder", "090", "b@mail.com", "HN");
        when(userDAO.findById(any(Connection.class), eq("seller-1"))).thenReturn(bidder);

        AuctionService service = new AuctionService(dataSource, auctionDAO, itemDAO, userDAO, imageStorage);
        CreateAuctionRequest request = createRequest("seller-1", LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        assertThrows(ValidationException.class, () -> service.createAuction(request));
    }

    @Test
    void createAuction_success_savesItemAndAuction() throws Exception {
        Seller seller = seller("seller-1");
        when(userDAO.findById(any(Connection.class), eq("seller-1"))).thenReturn(seller);
        when(itemDAO.saveItem(any(Connection.class), any(Item.class))).thenReturn(true);
        when(auctionDAO.saveAuction(any(Connection.class), any(Auction.class))).thenReturn(true);

        AuctionService service = new AuctionService(dataSource, auctionDAO, itemDAO, userDAO, imageStorage);
        CreateAuctionRequest request = createRequest("seller-1", LocalDateTime.now().plusMinutes(1), LocalDateTime.now().plusHours(2));

        service.createAuction(request);

        verify(itemDAO).saveItem(any(Connection.class), any(Item.class));
        verify(auctionDAO).saveAuction(any(Connection.class), any(Auction.class));
    }



    private static CreateAuctionRequest createRequest(String sellerId, LocalDateTime start, LocalDateTime end) {
        return new CreateAuctionRequest(
                sellerId,
                "Phone",
                "Desc",
                "item",
                100.0,
                start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
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

