package com.auction.server;

import com.auction.server.controller.RequestDispatcher;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.dao.AuctionDAOImpl;
import com.auction.server.dao.BidTransactionDAOImpl;
import com.auction.server.dao.ItemDAOImpl;
import com.auction.server.dao.UserDAOImpl;
import com.auction.server.mapper.UserMapper;
import com.auction.server.network.AuctionServer;
import com.auction.server.network.AuctionSubscriptionRegistry;
import com.auction.server.service.AuctionQueryService;
import com.auction.server.service.AuctionService;
import com.auction.server.service.AuctionStatusScheduler;
import com.auction.server.service.AutoBidRegistry;
import com.auction.server.service.AutoBidService;
import com.auction.server.service.BidBroadcastService;
import com.auction.server.service.BidService;
import com.auction.server.util.CloudinaryService;
import com.auction.server.service.UserService;
import com.auction.server.util.DatabaseConnection;
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

    UserDAO userDao = new UserDAOImpl();
    UserMapper userMapper = new UserMapper();
    ItemDAO itemDao = new ItemDAOImpl();
    AuctionDAO auctionDao = new AuctionDAOImpl();
    BidTransactionDAO bidTransactionDao = new BidTransactionDAOImpl();
    UserService userService = new UserService(userDao, bidTransactionDao, auctionDao, userMapper);

    AuctionSubscriptionRegistry subscriptionRegistry = new AuctionSubscriptionRegistry();
    BidBroadcastService bidBroadcastService = new BidBroadcastService(subscriptionRegistry);

    AuctionService auctionService = new AuctionService(auctionDao, itemDao, userDao);
    auctionService.setImageStorage(new CloudinaryService());

    BidService bidService = new BidService(auctionDao, bidTransactionDao, userDao, bidBroadcastService);
    AuctionQueryService auctionQueryService = new AuctionQueryService(auctionDao, bidTransactionDao);

    AutoBidRegistry autoBidRegistry = new AutoBidRegistry();
    AutoBidService autoBidService = new AutoBidService(autoBidRegistry, bidService, auctionQueryService, userDao);

    AuctionStatusScheduler auctionStatusScheduler = new AuctionStatusScheduler(auctionDao, auctionService, subscriptionRegistry, autoBidRegistry, 2000L);
    // chạy ngầm bộ đếm thời gian đấu giá (cập nhật trạng thái liên tục)
    backgroundExecutor.submit(auctionStatusScheduler);

    com.auction.server.service.BidCoordinator bidCoordinator = new com.auction.server.service.BidCoordinator(bidService, autoBidService);
    RequestDispatcher requestDispatcher =
        new RequestDispatcher(userService, auctionService, autoBidService, bidCoordinator, auctionQueryService, subscriptionRegistry);

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("Server start on port: " + port);
      while (!serverSocket.isClosed()) {
        Socket clientSocket = serverSocket.accept();
        clientExecutor.submit(new AuctionServer(clientSocket, requestDispatcher, subscriptionRegistry));}
      // giao việc xử lý client socket cho một thread độc lập trong pool
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      auctionStatusScheduler.shutdown();
      backgroundExecutor.shutdownNow();
      clientExecutor.shutdown();
      DatabaseConnection.shutdown();
    }
  }
}
