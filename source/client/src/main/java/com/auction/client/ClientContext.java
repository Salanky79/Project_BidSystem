package com.auction.client;

import com.auction.client.network.SocketClient;
import com.auction.client.service.AuctionService;
import com.auction.client.service.BidService;
import com.auction.client.service.UserService;

import java.io.InputStream;
import java.util.Properties;

public final class ClientContext {
  private static String SERVER_HOST = "localhost";
  private static int SERVER_PORT = 12345;

  static {
    try (InputStream input = ClientContext.class.getClassLoader().getResourceAsStream("config.properties")) {
      if (input != null) {
        Properties prop = new Properties();
        prop.load(input);
        SERVER_HOST = prop.getProperty("server.host", "localhost");
        SERVER_PORT = Integer.parseInt(prop.getProperty("server.port", "12345"));
      } else {
        System.err.println("Sorry, unable to find config.properties");
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private static final SocketClient SOCKET_CLIENT =
      new SocketClient(SERVER_HOST, SERVER_PORT);

  private static final UserService USER_SERVICE = new UserService(SOCKET_CLIENT);
  private static final AuctionService AUCTION_SERVICE = new AuctionService(SOCKET_CLIENT);
  private static final BidService BID_SERVICE = new BidService(SOCKET_CLIENT);

  private ClientContext() {}

  public static UserService userService() {
    return USER_SERVICE;
  }

  public static AuctionService auctionService() {
    return AUCTION_SERVICE;
  }

  public static BidService bidService() {
    return BID_SERVICE;
  }

  public static SocketClient socketClient() {
    return SOCKET_CLIENT;
  }
}
