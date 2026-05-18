package com.auction.server;

import com.auction.server.dao.UserDAO;
import com.auction.server.controller.RequestHandler;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.network.AuctionSubscriptionRegistry;
import com.auction.server.network.AuctionServer;
import com.auction.server.service.AuctionService;
import com.auction.server.service.AuctionStatusScheduler;
import com.auction.server.service.BidBroadcastService;
import com.auction.server.service.UserService;
import com.auction.server.util.DatabaseConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApplication {
    private static final ExecutorService clientExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private static final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private static final int port = Integer.parseInt(
            System.getenv().getOrDefault("PORT", "9999"));

    public static void main(String[] args) {
        UserDAO userDao = new UserDAO();
        UserService userService = new UserService(userDao);
        ItemDAO itemDao = new ItemDAO();
        AuctionDAO auctionDao = new AuctionDAO();
        BidTransactionDAO bidTransactionDao = new BidTransactionDAO();
        AuctionSubscriptionRegistry subscriptionRegistry = new AuctionSubscriptionRegistry();
        BidBroadcastService bidBroadcastService = new BidBroadcastService(subscriptionRegistry);
        AuctionService auctionService = new AuctionService( //
                auctionDao,
                itemDao,
                bidTransactionDao,
                userDao,
                bidBroadcastService
        );
        AuctionStatusScheduler auctionStatusScheduler = new AuctionStatusScheduler(auctionDao, 1000);
        // REALTIME DEALER
        backgroundExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    auctionStatusScheduler.run();
                } catch (RuntimeException e) {
                    System.err.println("Auction status scheduler failed, restarting: " + e.getMessage());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        RequestHandler requestHandler = new RequestHandler(userService, auctionService, subscriptionRegistry);

        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server start on port: " + port);
            while(!serverSocket.isClosed()){
                Socket clientSocket = serverSocket.accept();
                clientExecutor.submit(new AuctionServer(clientSocket, requestHandler, subscriptionRegistry));
            } // tao mot thread de xu ly client vua connect
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            auctionStatusScheduler.shutdown();
            backgroundExecutor.shutdownNow();
            clientExecutor.shutdown();
            DatabaseConnection.shutdown();
        }
    }
}
