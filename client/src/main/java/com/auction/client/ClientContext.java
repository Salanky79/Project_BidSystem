package com.auction.client;

import com.auction.client.network.SocketClient;
import com.auction.client.service.AuctionService;
import com.auction.client.service.BidService;
import com.auction.client.service.UserService;
import com.auction.client.session.SessionManager;

public final class ClientContext {
  private static final String SERVER_HOST = "maglev.proxy.rlwy.net";
  private static final int SERVER_PORT = 44658;

  private static final SessionManager SESSION_MANAGER = new SessionManager();
  private static final SocketClient SOCKET_CLIENT =
      new SocketClient(SERVER_HOST, SERVER_PORT, SESSION_MANAGER);

  private static final UserService USER_SERVICE = new UserService(SOCKET_CLIENT, SESSION_MANAGER);
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
}
