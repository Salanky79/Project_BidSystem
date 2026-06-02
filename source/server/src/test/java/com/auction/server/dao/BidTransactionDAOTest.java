package com.auction.server.dao;

import com.auction.share.enums.Category;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.auction.BidTransaction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class BidTransactionDAOTest {

    @Container
    public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_pass");

    private static Connection connection;
    private static BidTransactionDAO bidTransactionDAO;

    @BeforeAll
    static void setUp() throws Exception {
        String url = mysql.getJdbcUrl();
        String username = mysql.getUsername();
        String password = mysql.getPassword();
        connection = DriverManager.getConnection(url, username, password);
        bidTransactionDAO = new BidTransactionDAO();

        try (Statement stmt = connection.createStatement()) {
            // Create tables for test
            stmt.execute("CREATE TABLE users (id VARCHAR(36) PRIMARY KEY, fullname VARCHAR(255) NOT NULL, username VARCHAR(100) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL, phoneNumber VARCHAR(20), email VARCHAR(100) UNIQUE, role VARCHAR(20) NOT NULL, balance DOUBLE DEFAULT 0.0, address VARCHAR(255), access_level INT DEFAULT 0)");
            stmt.execute("CREATE TABLE items (id VARCHAR(36) PRIMARY KEY, seller_id VARCHAR(36) NOT NULL, name VARCHAR(255) NOT NULL, category VARCHAR(50) NOT NULL, starting_price DOUBLE NOT NULL, description TEXT, image_url VARCHAR(500), FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE)");
            stmt.execute("CREATE TABLE auctions (id VARCHAR(36) PRIMARY KEY, item_id VARCHAR(36) NOT NULL, seller_id VARCHAR(36) NOT NULL, current_price DOUBLE NOT NULL, highest_bidder_id VARCHAR(36), start_time TIMESTAMP NOT NULL, end_time TIMESTAMP NOT NULL, bid_step DOUBLE NOT NULL, status VARCHAR(20) NOT NULL, bid_count INT DEFAULT 0, FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE, FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY (highest_bidder_id) REFERENCES users(id) ON DELETE SET NULL)");
            stmt.execute("CREATE TABLE bid_transactions (id VARCHAR(36) PRIMARY KEY, auction_id VARCHAR(36) NOT NULL, bidder_id VARCHAR(36) NOT NULL, amount DOUBLE NOT NULL, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE, FOREIGN KEY (bidder_id) REFERENCES users(id) ON DELETE CASCADE)");
            
            // Insert seed data
            stmt.execute("INSERT INTO users (id, fullname, username, password, role) VALUES ('s-1', 'Seller', 'seller', 'pass', 'SELLER')");
            stmt.execute("INSERT INTO users (id, fullname, username, password, role) VALUES ('b-1', 'Bidder', 'bidder', 'pass', 'BIDDER')");
            stmt.execute("INSERT INTO items (id, seller_id, name, category, starting_price) VALUES ('i-1', 's-1', 'Item', 'ITEM', 100)");
            stmt.execute("INSERT INTO auctions (id, item_id, seller_id, current_price, start_time, end_time, bid_step, status) VALUES ('a-1', 'i-1', 's-1', 100, CURRENT_TIMESTAMP, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 HOUR), 10, 'RUNNING')");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void testSaveAndFindByAuction() throws Exception {
        Seller seller = new Seller("s", "p", "Seller", "0", "s@mail", "Addr");
        seller.setID("s-1");
        Item item = new Item("Item", "D", 100, "s-1", Category.ITEM);
        item.setID("i-1");
        Auction auction = new Auction(item, seller, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        auction.setID("a-1");
        
        Bidder bidder = new Bidder("b", "p", "Bidder", "0", "b@mail", "Addr");
        bidder.setID("b-1");

        BidTransaction transaction = new BidTransaction(auction, bidder, 150.0);
        transaction.setTimestamp(LocalDateTime.now());
        
        boolean saved = bidTransactionDAO.saveBidTransaction(connection, transaction);
        assertTrue(saved);

        List<BidTransaction> list = bidTransactionDAO.findByAuction(connection, auction);
        assertEquals(1, list.size());
        assertEquals(150.0, list.get(0).getAmount());
        assertEquals("b-1", list.get(0).getBidder().getId());
    }
}
