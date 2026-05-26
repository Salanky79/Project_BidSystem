package com.auction.server;

import com.auction.server.controller.RequestHandler;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.network.AuctionServer;
import com.auction.server.network.AuctionSubscriptionRegistry;
import com.auction.server.service.AuctionService;
import com.auction.server.service.AuctionStatusScheduler;
import com.auction.server.service.AutoBidRegistry;
import com.auction.server.service.AutoBidService;
import com.auction.server.service.BidBroadcastService;
import com.auction.server.service.CloudinaryService;
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
    UserDAO userDao = new UserDAO();
    UserService userService = new UserService(userDao);
    ItemDAO itemDao = new ItemDAO();
    AuctionDAO auctionDao = new AuctionDAO();
    BidTransactionDAO bidTransactionDao = new BidTransactionDAO();
    AuctionSubscriptionRegistry subscriptionRegistry = new AuctionSubscriptionRegistry();
    BidBroadcastService bidBroadcastService = new BidBroadcastService(subscriptionRegistry);
    AutoBidRegistry autoBidRegistry = new AutoBidRegistry();
    AuctionService auctionService =
        new AuctionService( //
            auctionDao, itemDao, bidTransactionDao, userDao, bidBroadcastService, null);
    AutoBidService autoBidService = new AutoBidService(autoBidRegistry, auctionService);
    auctionService.setAutoBidService(autoBidService);
    auctionService.setCloudinaryService(new CloudinaryService());
    AuctionStatusScheduler auctionStatusScheduler = new AuctionStatusScheduler(auctionDao, 3000);
    // chạy ngầm bộ đếm thời gian đấu giá (cập nhật trạng thái liên tục)
    backgroundExecutor.submit(
        () -> {
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

    RequestHandler requestHandler =
        new RequestHandler(userService, auctionService, autoBidService, subscriptionRegistry);

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("Server start on port: " + port);
      while (!serverSocket.isClosed()) {
        Socket clientSocket = serverSocket.accept();
        clientExecutor.submit(
            new AuctionServer(clientSocket, requestHandler, subscriptionRegistry));
      } // giao việc xử lý client socket cho một thread độc lập trong pool
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
