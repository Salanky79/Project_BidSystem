package com.auction.server;

import com.auction.server.controller.AuctionController;
import com.auction.server.controller.RequestDispatcher;
import com.auction.server.controller.UserController;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.mapper.UserMapper;
import com.auction.server.network.ClientConnectionHandler;
import com.auction.server.service.AuctionSubscriptionRegistry;
import com.auction.server.service.*;
import com.auction.server.util.CloudinaryService;
import com.auction.server.util.DatabaseConnection;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Lớp khởi chạy hệ thống Server, cấu hình các service, thread pool và socket lắng nghe. */
public class ServerApplication {
  // thread pool tự tạo thêm luồng khi cần và tái sử dụng luồng nhàn rỗi cho Client

  private static final ExecutorService clientExecutor = Executors.newCachedThreadPool();
  // thread nền duy nhất dành cho việc chạy vòng lặp Scheduler
  private static final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
  private static final int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

  public static void main(String[] args) throws SQLException {
    // === Database Initialization ===
    HikariDataSource dataSource = DatabaseConnection.createDataSource();

    // === DAO Layer ===
    UserDAO userDao = new UserDAO();
    UserMapper userMapper = new UserMapper();
    ItemDAO itemDao = new ItemDAO();
    AuctionDAO auctionDao = new AuctionDAO();
    BidTransactionDAO bidTransactionDao = new BidTransactionDAO();
    
    // === Core Service Layer ===
    UserService userService = new UserService(dataSource, userDao, bidTransactionDao, auctionDao, userMapper);
    
    AuctionService auctionService = new AuctionService(dataSource, auctionDao, itemDao, userDao,new CloudinaryService());
    
    AuctionQueryService auctionQueryService = new AuctionQueryService(dataSource, auctionDao, bidTransactionDao);

    // === Network / Broadcast Service Layer ===
    AuctionSubscriptionRegistry subscriptionRegistry = new AuctionSubscriptionRegistry();
    BroadcastService bidBroadcastService = new BroadcastService(subscriptionRegistry);
    
    BidService bidService = new BidService(dataSource, auctionDao, bidTransactionDao, userDao, bidBroadcastService);
    
    AutoBidRegistry autoBidRegistry = new AutoBidRegistry();
    AutoBidService autoBidService = new AutoBidService(
        dataSource, autoBidRegistry, bidService, userDao, auctionDao, bidBroadcastService);
    bidService.setAutoBidService(autoBidService);
    
    // === Background Schedulers ===
    AuctionStatusScheduler auctionStatusScheduler = new AuctionStatusScheduler(auctionService, 2000L);
    auctionStatusScheduler.addListener(bidBroadcastService);
    auctionStatusScheduler.addListener(autoBidRegistry);
    // chạy ngầm bộ đếm thời gian đấu giá (cập nhật trạng thái liên tục)
    backgroundExecutor.submit(auctionStatusScheduler);

    // === Controllers / Request Dispatching ===
    UserController userController = new UserController(userService, new UserMapper());
    AuctionController auctionController = new AuctionController(auctionService, autoBidService, bidService, auctionQueryService, bidBroadcastService);
    RequestDispatcher requestDispatcher = new RequestDispatcher(userController, auctionController, subscriptionRegistry);

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("Server start on port: " + port);
      while (!serverSocket.isClosed()) {
        Socket clientSocket = serverSocket.accept();
        clientExecutor.submit(new ClientConnectionHandler(clientSocket, requestDispatcher, subscriptionRegistry));
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      auctionStatusScheduler.shutdown();
      autoBidService.shutdown();
      bidBroadcastService.shutdown();
      backgroundExecutor.shutdownNow();
      clientExecutor.shutdown();
      dataSource.close();
    }
  }
}


