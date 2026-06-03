package com.auction.server.dao;

import com.auction.share.enums.AuctionStatus;
import com.auction.share.enums.Category;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Seller;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Testcontainers
class AuctionDAOTest {

    @Container
    public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_pass");

    private static Connection connection;
    private static AuctionDAO auctionDAO;

    @BeforeAll
    static void setUpAll() throws Exception {
        connection = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
        auctionDAO = new AuctionDAO();

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE users (" +
                    "id VARCHAR(36) PRIMARY KEY," +
                    "fullname VARCHAR(255) NOT NULL," +
                    "username VARCHAR(100) UNIQUE NOT NULL," +
                    "password VARCHAR(255) NOT NULL," +
                    "phoneNumber VARCHAR(20)," +
                    "email VARCHAR(100) UNIQUE," +
                    "role VARCHAR(20) NOT NULL," +
                    "balance DOUBLE DEFAULT 0.0," +
                    "address VARCHAR(255)," +
                    "access_level INT DEFAULT 0)");

            stmt.execute("CREATE TABLE items (" +
                    "id VARCHAR(36) PRIMARY KEY," +
                    "seller_id VARCHAR(36) NOT NULL," +
                    "name VARCHAR(255) NOT NULL," +
                    "category VARCHAR(50) NOT NULL," +
                    "starting_price DOUBLE NOT NULL," +
                    "description TEXT," +
                    "image_url TEXT," +
                    "FOREIGN KEY (seller_id) REFERENCES users(id))");

            stmt.execute("CREATE TABLE auctions (" +
                    "id VARCHAR(36) PRIMARY KEY," +
                    "item_id VARCHAR(36) NOT NULL," +
                    "seller_id VARCHAR(36) NOT NULL," +
                    "current_price DOUBLE NOT NULL," +
                    "highest_bidder_id VARCHAR(36)," +
                    "start_time TIMESTAMP NOT NULL," +
                    "end_time TIMESTAMP NOT NULL," +
                    "bid_step DOUBLE NOT NULL," +
                    "status VARCHAR(20) NOT NULL," +
                    "bid_count INT DEFAULT 0," +
                    "FOREIGN KEY (item_id) REFERENCES items(id)," +
                    "FOREIGN KEY (seller_id) REFERENCES users(id)," +
                    "FOREIGN KEY (highest_bidder_id) REFERENCES users(id))");

            stmt.execute("INSERT INTO users (id, fullname, username, password, role) VALUES ('s-1', 'Seller', 's1', 'pass', 'SELLER')");
            stmt.execute("INSERT INTO users (id, fullname, username, password, role) VALUES ('b-1', 'Bidder', 'b1', 'pass', 'BIDDER')");
        }
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @BeforeEach
    void clearAuctions() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM auctions");
            stmt.execute("DELETE FROM items");
        }
    }

    private Auction createAndSaveAuction(String id, String sellerId, String itemId, AuctionStatus status) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO items (id, seller_id, name, category, starting_price) VALUES ('" + itemId + "', '" + sellerId + "', 'Test Item', 'ITEM', 100)");
        }
        
        Seller seller = new Seller("s1", "pass", "Seller", "090", "a@mail", "addr");
        seller.setID(sellerId);
        Item item = new Item("Test Item", "Desc", 100.0, sellerId, Category.ITEM);
        item.setID(itemId);
        
        LocalDateTime start = LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS);
        
        Auction auction = new Auction(item, seller, start, end);
        auction.setID(id);
        
        // Force status
        if (status == AuctionStatus.RUNNING) auction.markRunning();
        else if (status == AuctionStatus.FINISHED) auction.markFinished();
        else if (status == AuctionStatus.CANCELED) auction.markCanceled();
        
        auctionDAO.saveAuction(connection, auction);
        return auction;
    }

    @Test
    void saveAndFindAuction() throws Exception {
        Auction auction = createAndSaveAuction("a-1", "s-1", "i-1", AuctionStatus.RUNNING);
        
        Auction found = auctionDAO.findById(connection, "a-1");
        assertNotNull(found);
        assertEquals(AuctionStatus.RUNNING, found.getStatus());
        assertEquals("s-1", found.getSeller().getId());
        assertEquals("Test Item", found.getItem().getName());
        assertEquals(100.0, found.getCurrentHighestBid());
    }

    @Test
    void updateStatus() throws Exception {
        createAndSaveAuction("a-1", "s-1", "i-1", AuctionStatus.RUNNING);
        
        boolean updated = auctionDAO.updateStatus(connection, "a-1", AuctionStatus.FINISHED);
        assertTrue(updated);
        
        Auction found = auctionDAO.findById(connection, "a-1");
        assertEquals(AuctionStatus.FINISHED, found.getStatus());
    }

    @Test
    void sumAuctionCurrentPrices() throws Exception {
        createAndSaveAuction("a-1", "s-1", "i-1", AuctionStatus.RUNNING);
        createAndSaveAuction("a-2", "s-1", "i-2", AuctionStatus.RUNNING);
        
        // Set b-1 as highest bidder for a-1 and a-2
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        auctionDAO.updateHighestBid(connection, "a-1", "b-1", 150.0, now.plusHours(1), now);
        auctionDAO.updateHighestBid(connection, "a-2", "b-1", 200.0, now.plusHours(1), now);
        
        double total = auctionDAO.sumAuctionCurrentPrices(connection, "b-1", null);
        assertEquals(350.0, total);
        
        double totalExclude = auctionDAO.sumAuctionCurrentPrices(connection, "b-1", "a-1");
        assertEquals(200.0, totalExclude);
    }
    
    @Test
    void markOpenAuctionsAsRunning() throws Exception {
        // Create an auction with start time in the past, but status is OPEN
        Seller seller = new Seller("s1", "pass", "Seller", "0", "a@a", "a");
        seller.setID("s-1");
        Item item = new Item("I", "D", 100, "s-1", Category.ITEM);
        item.setID("i-1");
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO items (id, seller_id, name, category, starting_price) VALUES ('i-1', 's-1', 'I', 'ITEM', 100)");
        }
        
        // start time is in the past, so it should be running
        LocalDateTime start = LocalDateTime.now().minusMinutes(10).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS);
        
        // Use insert directly to force OPEN status for testing
        try (var ps = connection.prepareStatement("INSERT INTO auctions (id, item_id, seller_id, current_price, start_time, end_time, bid_step, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, "a-1");
            ps.setString(2, "i-1");
            ps.setString(3, "s-1");
            ps.setDouble(4, 100);
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(start));
            ps.setTimestamp(6, java.sql.Timestamp.valueOf(end));
            ps.setDouble(7, 10);
            ps.setString(8, AuctionStatus.OPEN.name());
            ps.executeUpdate();
        }
        
        int marked = auctionDAO.markOpenAuctionsAsRunning(connection, LocalDateTime.now());
        assertEquals(1, marked);
        
        Auction found = auctionDAO.findById(connection, "a-1");
        assertEquals(AuctionStatus.RUNNING, found.getStatus());
    }

    @Test
    void finishAuctions() throws Exception {
        createAndSaveAuction("a-1", "s-1", "i-1", AuctionStatus.RUNNING);
        createAndSaveAuction("a-2", "s-1", "i-2", AuctionStatus.RUNNING);
        
        int finished = auctionDAO.finishAuctions(connection, List.of("a-1", "a-2"));
        assertEquals(2, finished);
        
        assertEquals(AuctionStatus.FINISHED, auctionDAO.findById(connection, "a-1").getStatus());
        assertEquals(AuctionStatus.FINISHED, auctionDAO.findById(connection, "a-2").getStatus());
    }
}
