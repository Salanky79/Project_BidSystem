package com.auction.server.dao;

import com.auction.share.enums.Category;
import com.auction.share.models.item.Item;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
class ItemDAOTest {

    @Container
    public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_pass");

    private static Connection connection;
    private static ItemDAO itemDAO;

    @BeforeAll
    static void setUp() throws Exception {
        String url = mysql.getJdbcUrl();
        String username = mysql.getUsername();
        String password = mysql.getPassword();
        connection = DriverManager.getConnection(url, username, password);
        itemDAO = new ItemDAO();

        try (Statement stmt = connection.createStatement()) {
            // users table for foreign key constraint
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

            // items table
            stmt.execute("CREATE TABLE items (" +
                    "id VARCHAR(36) PRIMARY KEY," +
                    "seller_id VARCHAR(36) NOT NULL," +
                    "name VARCHAR(255) NOT NULL," +
                    "category VARCHAR(50) NOT NULL," +
                    "starting_price DOUBLE NOT NULL," +
                    "description TEXT," +
                    "image_url TEXT," +
                    "FOREIGN KEY (seller_id) REFERENCES users(id))");

            // Seed user
            stmt.execute("INSERT INTO users (id, fullname, username, password, role) VALUES ('s-1', 'Seller', 's1', 'pass', 'SELLER')");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void saveAndFindItem() throws Exception {
        Item item = new Item("Laptop", "Gaming laptop", 1000.0, "s-1", Category.ELECTRONIC);
        item.setID("item-1");
        item.setImageUrl("http://image.url");

        boolean saved = itemDAO.saveItem(connection, item);
        assertTrue(saved);

        Item found = itemDAO.findById(connection, "item-1");
        assertNotNull(found);
        assertEquals("Laptop", found.getName());
        assertEquals(Category.ELECTRONIC, found.getCategory());
        assertEquals(1000.0, found.getStartingPrice());
        assertEquals("Gaming laptop", found.getDescription());
        assertEquals("http://image.url", found.getImageUrl());
        assertEquals("s-1", found.getSellerId());
    }

    @Test
    void findById_notFound() throws Exception {
        Item found = itemDAO.findById(connection, "not-exist");
        assertNull(found);
    }
}
