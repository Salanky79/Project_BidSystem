package com.auction.client;

import com.auction.client.network.AuctionNetwork;
import com.auction.client.network.BidNetwork;
import com.auction.client.network.SocketClient;
import com.auction.client.network.UserNetwork;
import com.auction.client.session.SessionManager;
import com.auction.client.service.AuctionService;
import com.auction.client.service.BidService;
import com.auction.client.service.UserService;

public final class ClientContext {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    private static final SessionManager SESSION_MANAGER = new SessionManager();
    private static final SocketClient SOCKET_CLIENT = new SocketClient(SERVER_HOST, SERVER_PORT, SESSION_MANAGER);
    private static final UserNetwork USER_NETWORK = new UserNetwork(SOCKET_CLIENT);
    private static final AuctionNetwork AUCTION_NETWORK = new AuctionNetwork(SOCKET_CLIENT);
    private static final BidNetwork BID_NETWORK = new BidNetwork(SOCKET_CLIENT);

    private static final UserService USER_SERVICE = new UserService(USER_NETWORK, SESSION_MANAGER);
    private static final AuctionService AUCTION_SERVICE = new AuctionService(AUCTION_NETWORK);
    private static final BidService BID_SERVICE = new BidService(BID_NETWORK);

    private ClientContext() {
    }

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
