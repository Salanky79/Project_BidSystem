package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.share.DTO.*;
import com.auction.share.enums.AuctionStatus;
import com.auction.share.enums.Category;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.auction.BidTransaction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuctionServiceLogicTest {

    @Test
    void placeBidHappyPathShouldSaveAndBroadcast() throws Exception {
        Auction auction = buildRunningAuction(100);
        Bidder bidder = buildBidder("bidder-1", "Bidder One");

        FakeAuctionDAO auctionDAO = new FakeAuctionDAO();
        auctionDAO.findByIdResult = auction;
        auctionDAO.updateHighestBidResult = true;

        FakeBidTransactionDAO bidTxDAO = new FakeBidTransactionDAO();
        FakeUserDAO userDAO = new FakeUserDAO();
        userDAO.findByIdResult = bidder;
        FakeBidBroadcastService broadcast = new FakeBidBroadcastService();

        AuctionService service = new TestableAuctionService(
                auctionDAO, new ItemDAO(), bidTxDAO, userDAO, broadcast, fakeConnection()
        );

        boolean result = service.placeBid(new PlaceBidRequest(auction.getId(), bidder.getId(), 150));

        assertTrue(result);
        assertTrue(auctionDAO.updateCalled);
        assertTrue(bidTxDAO.saveCalled);
        assertNotNull(broadcast.lastEvent);
        assertEquals(auction.getId(), broadcast.lastEvent.getAuctionId());
        assertEquals(bidder.getId(), broadcast.lastEvent.getBidderId());
        assertEquals(150, broadcast.lastEvent.getAmount());
    }

    @Test
    void placeBidShouldThrowWhenAtomicUpdateFailsDueToConcurrentChange() {
        // atomic update fail => place bid lỗi
        Auction auction = buildRunningAuction(100);
        Bidder bidder = buildBidder("bidder-1", "Bidder One");

        FakeAuctionDAO auctionDAO = new FakeAuctionDAO();
        auctionDAO.findByIdResult = auction;
        // người khác update trước và cao hơn giá bidder chuẩn bị
        auctionDAO.updateHighestBidResult = false;

        FakeBidTransactionDAO bidTxDAO = new FakeBidTransactionDAO();
        FakeUserDAO userDAO = new FakeUserDAO();
        userDAO.findByIdResult = bidder;

        AuctionService service = new TestableAuctionService(
                auctionDAO, new ItemDAO(), bidTxDAO, userDAO, new FakeBidBroadcastService(), fakeConnection()
        );

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.placeBid(new PlaceBidRequest(auction.getId(), bidder.getId(), 160))
        );

        assertTrue(ex.getMessage().contains("Bid rejected"));
        assertFalse(bidTxDAO.saveCalled);
    }

    @Test
    void placeBidShouldRollbackAndRethrowWhenSavingTransactionFails() {
        Auction auction = buildRunningAuction(100);
        Bidder bidder = buildBidder("bidder-1", "Bidder One");

        FakeAuctionDAO auctionDAO = new FakeAuctionDAO();
        auctionDAO.findByIdResult = auction;
        // update thành công
        auctionDAO.updateHighestBidResult = true;

        FakeBidTransactionDAO bidTxDAO = new FakeBidTransactionDAO();
        // giả lập fail
        bidTxDAO.throwOnSave = true;
        FakeUserDAO userDAO = new FakeUserDAO();
        userDAO.findByIdResult = bidder;
        FakeBidBroadcastService broadcast = new FakeBidBroadcastService();

        SQLException ex = assertThrows(
                SQLException.class,
                () -> new TestableAuctionService(
                        auctionDAO, new ItemDAO(), bidTxDAO, userDAO, broadcast, fakeConnection()
                ).placeBid(new PlaceBidRequest(auction.getId(), bidder.getId(), 150))
        );

        // save fail
        assertEquals("db-write-failed", ex.getMessage());
        // fail => ko broadcast
        assertNull(broadcast.lastEvent);
    }

    @Test
    void placeBidShouldThrowWhenBidderNotFound() {
        Auction auction = buildRunningAuction(100);
        FakeAuctionDAO auctionDAO = new FakeAuctionDAO();
        auctionDAO.findByIdResult = auction;

        FakeUserDAO userDAO = new FakeUserDAO();
        userDAO.findByIdResult = null;

        AuctionService service = new TestableAuctionService(
                auctionDAO, new ItemDAO(), new FakeBidTransactionDAO(), userDAO, new FakeBidBroadcastService(), fakeConnection()
        );

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.placeBid(new PlaceBidRequest(auction.getId(), "missing-bidder", 120))
        );
        assertEquals("User is not a bidder.", ex.getMessage());
    }

    @Test
    void placeBidBoundaryAtExactStartShouldPassPrechecks() throws Exception {
        Seller seller = new Seller("seller", "pwd", "Seller", "091", "s@mail.com", "HCM");
        seller.setID("seller-1");
        Item item = new Item("Item", "Desc", 100, seller.getId(), Category.ITEM);
        LocalDateTime now = LocalDateTime.now();
        Auction auction = new Auction(item, seller, now, now.plusMinutes(1));

        Bidder bidder = buildBidder("bidder-1", "Bidder One");
        FakeAuctionDAO auctionDAO = new FakeAuctionDAO();
        auctionDAO.findByIdResult = auction;
        auctionDAO.updateHighestBidResult = true;
        FakeBidTransactionDAO bidTxDAO = new FakeBidTransactionDAO();
        FakeUserDAO userDAO = new FakeUserDAO();
        userDAO.findByIdResult = bidder;

        boolean ok = new TestableAuctionService(
                auctionDAO, new ItemDAO(), bidTxDAO, userDAO, new FakeBidBroadcastService(), fakeConnection()
        ).placeBid(new PlaceBidRequest(auction.getId(), bidder.getId(), 120));

        assertTrue(ok);
    }

    @Test
    void placeBidBoundaryAtExactEndShouldFail() {
        Seller seller = new Seller("seller", "pwd", "Seller", "091", "s@mail.com", "HCM");
        seller.setID("seller-1");
        Item item = new Item("Item", "Desc", 100, seller.getId(), Category.ITEM);
        LocalDateTime now = LocalDateTime.now();
        Auction auction = new Auction(item, seller, now.minusMinutes(1), now);

        Bidder bidder = buildBidder("bidder-1", "Bidder One");
        FakeAuctionDAO auctionDAO = new FakeAuctionDAO();
        auctionDAO.findByIdResult = auction;
        FakeUserDAO userDAO = new FakeUserDAO();
        userDAO.findByIdResult = bidder;

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> new TestableAuctionService(
                        auctionDAO, new ItemDAO(), new FakeBidTransactionDAO(), userDAO, new FakeBidBroadcastService(), fakeConnection()
                ).placeBid(new PlaceBidRequest(auction.getId(), bidder.getId(), 120))
        );
        assertEquals("Auction is not running.", ex.getMessage());
    }

    @Test
    void createAuctionShouldValidateSellerAndTimeAndCategoryAndSave() throws Exception {
        Seller seller = new Seller("s1", "pwd", "Seller 1", "090", "s@mail.com", "HCM");
        seller.setID("seller-1");

        FakeAuctionDAO auctionDAO = new FakeAuctionDAO();
        FakeUserDAO userDAO = new FakeUserDAO();
        userDAO.findByIdResult = seller;

        AuctionService service = new TestableAuctionService(
                auctionDAO, new ItemDAO(), new FakeBidTransactionDAO(), userDAO, new FakeBidBroadcastService(), fakeConnection()
        );

        LocalDateTime start = LocalDateTime.now().plusMinutes(5);
        LocalDateTime end = start.plusHours(1);
        CreateAuctionRequest okReq = new CreateAuctionRequest(
                seller.getId(), "Phone", "Desc", "electronic", 1000,
                start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        Auction created = service.createAuction(okReq);
        assertNotNull(created);
        assertTrue(auctionDAO.saveCalled);

        CreateAuctionRequest badTimeReq = new CreateAuctionRequest(
                seller.getId(), "Phone", "Desc", "electronic", 1000,
                end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        assertThrows(ValidationException.class, () -> service.createAuction(badTimeReq));

        CreateAuctionRequest badCategoryReq = new CreateAuctionRequest(
                seller.getId(), "Phone", "Desc", "not_exist", 1000,
                start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        assertThrows(IllegalArgumentException.class, () -> service.createAuction(badCategoryReq));

        userDAO.findByIdResult = buildBidder("b2", "Bidder");
        assertThrows(ValidationException.class, () -> service.createAuction(okReq));

        CreateAuctionRequest badDateReq = new CreateAuctionRequest(
                seller.getId(), "Phone", "Desc", "electronic", 1000,
                "bad-date", "still-bad"
        );
        ValidationException ex = assertThrows(ValidationException.class, () -> service.createAuction(badDateReq));
        assertEquals("Invalid date format. Please use ISO format.", ex.getMessage());
    }

    @Test
    void getAuctionDetailShouldMapBidHistoryAndHandleNoHighestBidder() throws Exception {
        Auction auction = buildRunningAuction(100);
        auction.setID("auction-1");

        Bidder bidder = buildBidder("bidder-1", "Bidder One");
        BidTransaction t1 = new BidTransaction(auction, bidder, 120);
        t1.setTimestamp(LocalDateTime.of(2026, 5, 21, 10, 0));
        BidTransaction t2 = new BidTransaction(auction, bidder, 140);
        t2.setTimestamp(LocalDateTime.of(2026, 5, 21, 10, 5));

        FakeAuctionDAO auctionDAO = new FakeAuctionDAO();
        auctionDAO.findByIdResult = auction;
        FakeBidTransactionDAO bidTxDAO = new FakeBidTransactionDAO();
        bidTxDAO.findByAuctionResult = List.of(t1, t2);

        AuctionService service = new TestableAuctionService(
                auctionDAO, new ItemDAO(), bidTxDAO, new FakeUserDAO(), new FakeBidBroadcastService(), fakeConnection()
        );

        AuctionDetailDTO dto = service.getAuctionDetail("auction-1");
        assertEquals("auction-1", dto.getAuctionId());
        assertNull(dto.getHighestBidderName());
        assertEquals(2, dto.getBidHistory().size());
        assertEquals("Bidder One", dto.getBidHistory().get(0).getBidderName());
        assertEquals("2026-05-21T10:00:00", dto.getBidHistory().get(0).getTimestamp());

        auctionDAO.findByIdResult = null;
        assertThrows(ValidationException.class, () -> service.getAuctionDetail("missing"));
    }

    @Test
    void listAuctionsShouldUseStatusFilterOrFallbackToAll() throws Exception {
        Auction running = buildRunningAuction(100);
        running.markRunning();
        Auction open = buildRunningAuction(80);

        FakeAuctionDAO auctionDAO = new FakeAuctionDAO();
        auctionDAO.findByStatusResult = List.of(running);
        auctionDAO.findAllResult = List.of(running, open);

        AuctionService service = new TestableAuctionService(
                auctionDAO, new ItemDAO(), new FakeBidTransactionDAO(), new FakeUserDAO(), new FakeBidBroadcastService(), fakeConnection()
        );

        List<AuctionSummaryDTO> filtered = service.listAuctions(new ListAuctionRequest("running"));
        assertEquals(1, filtered.size());
        assertTrue(auctionDAO.findByStatusCalled);

        List<AuctionSummaryDTO> fallback = service.listAuctions(new ListAuctionRequest("invalid_status"));
        assertEquals(2, fallback.size());
        assertTrue(auctionDAO.findAllCalled);

        List<AuctionSummaryDTO> nullReq = service.listAuctions(null);
        assertEquals(2, nullReq.size());

        List<AuctionSummaryDTO> blankStatus = service.listAuctions(new ListAuctionRequest("   "));
        assertEquals(2, blankStatus.size());
    }

    private static Auction buildRunningAuction(double startingPrice) {
        Seller seller = new Seller("seller", "pwd", "Seller", "091", "s@mail.com", "HCM");
        seller.setID("seller-1");
        Item item = new Item("Item", "Desc", startingPrice, seller.getId(), Category.ITEM);
        return new Auction(item, seller, LocalDateTime.now().minusMinutes(5), LocalDateTime.now().plusMinutes(30));
    }

    private static Bidder buildBidder(String id, String name) {
        Bidder bidder = new Bidder("bidder", "pwd", name, "090", "b@mail.com", "HN");
        bidder.setID(id);
        return bidder;
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

    private static class TestableAuctionService extends AuctionService {
        private final Connection connection;

        private TestableAuctionService(
                AuctionDAO auctionDAO,
                ItemDAO itemDAO,
                BidTransactionDAO bidTransactionDAO,
                UserDAO userDAO,
                BidBroadcastService bidBroadcastService,
                Connection connection
        ) {
            super(auctionDAO, itemDAO, bidTransactionDAO, userDAO, bidBroadcastService);
            this.connection = connection;
        }

        @Override
        protected Connection getConnection() {
            return connection;
        }
    }

    private static class FakeAuctionDAO extends AuctionDAO {
        private Auction findByIdResult;
        private boolean updateHighestBidResult;
        private List<Auction> findByStatusResult = new ArrayList<>();
        private List<Auction> findAllResult = new ArrayList<>();
        private boolean saveCalled;
        private boolean updateCalled;
        private boolean findByStatusCalled;
        private boolean findAllCalled;

        @Override
        public Auction findById(String id) {
            return findByIdResult;
        }

        @Override
        public boolean saveAuction(Auction auction) {
            saveCalled = true;
            return true;
        }

        @Override
        public boolean updateHighestBidIfHigher(Connection conn, String id, String bidderId, double amount) {
            updateCalled = true;
            return updateHighestBidResult;
        }

        @Override
        public List<Auction> findByStatus(AuctionStatus status) {
            findByStatusCalled = true;
            return findByStatusResult;
        }

        @Override
        public List<Auction> findAll() {
            findAllCalled = true;
            return findAllResult;
        }
    }

    private static class FakeBidTransactionDAO extends BidTransactionDAO {
        private boolean saveCalled;
        private boolean throwOnSave;
        private List<BidTransaction> findByAuctionResult = new ArrayList<>();

        @Override
        public boolean saveBidTransaction(Connection conn, BidTransaction transaction) throws SQLException {
            saveCalled = true;
            if (throwOnSave) {
                throw new SQLException("db-write-failed");
            }
            return true;
        }

        @Override
        public List<BidTransaction> findByAuction(Auction auction) {
            return findByAuctionResult;
        }
    }

    private static class FakeUserDAO extends UserDAO {
        private User findByIdResult;

        @Override
        public User findById(String id) {
            return findByIdResult;
        }
    }

    private static class FakeBidBroadcastService extends BidBroadcastService {
        private BidUpdateEvent lastEvent;

        private FakeBidBroadcastService() {
            super(null);
        }

        @Override
        public void broadcastBidUpdate(BidUpdateEvent event) {
            lastEvent = event;
        }
    }
}
