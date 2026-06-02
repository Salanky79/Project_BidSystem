package com.auction.server.dao;

import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class UserDAOTest {

    @Container
    public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_pass");

    private static Connection connection;
    private static UserDAO userDAO;

    @BeforeAll
    static void setUp() throws Exception {
        String url = mysql.getJdbcUrl();
        String username = mysql.getUsername();
        String password = mysql.getPassword();
        connection = DriverManager.getConnection(url, username, password);
        userDAO = new UserDAO();

        // Init Schema
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
            
            stmt.execute("INSERT INTO users (id, fullname, username, password, role, balance) " +
                         "VALUES ('u-1', 'Test User', 'testuser', 'pass', 'BIDDER', 1000.0)");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void testFindById() throws Exception {
        User user = userDAO.findById(connection, "u-1");
        assertNotNull(user);
        assertEquals("Test User", user.getFullName());
        assertTrue(user instanceof Bidder);
        assertEquals(1000.0, ((Bidder) user).getBalance());
    }

    @Test
    void testFindBalanceForUpdate_PessimisticLocking() throws Exception {
        // Test pessimistic locking logic using multiple threads
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        
        AtomicReference<Double> balanceThread1 = new AtomicReference<>();
        AtomicReference<Double> balanceThread2 = new AtomicReference<>();

        executor.submit(() -> {
            try (Connection conn1 = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())) {
                conn1.setAutoCommit(false);
                // Acquire lock
                balanceThread1.set(userDAO.findBalanceForUpdate(conn1, "u-1"));
                
                // Signal thread 2 to start
                latch1.countDown();
                
                // Wait a bit to simulate work holding the lock
                Thread.sleep(1000);
                
                // Update balance directly
                try (java.sql.PreparedStatement ps = conn1.prepareStatement("UPDATE users SET balance = ? WHERE id = ?")) {
                    ps.setDouble(1, balanceThread1.get() - 100);
                    ps.setString(2, "u-1");
                    ps.executeUpdate();
                }
                conn1.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        executor.submit(() -> {
            try {
                // Wait for thread 1 to acquire lock
                latch1.await();
                
                try (Connection conn2 = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())) {
                    conn2.setAutoCommit(false);
                    // This will block until Thread 1 commits!
                    balanceThread2.set(userDAO.findBalanceForUpdate(conn2, "u-1"));
                    conn2.commit();
                    latch2.countDown();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Wait for both to finish
        latch2.await();
        executor.shutdown();

        // Thread 1 read 1000
        assertEquals(1000.0, balanceThread1.get());
        
        // Thread 2 should read 900 because it had to wait for Thread 1 to finish its transaction!
        assertEquals(900.0, balanceThread2.get());
    }
}
