package com.auction.client;

import com.auction.client.network.SocketClient;
import com.auction.client.session.SessionManager;
import com.auction.client.service.AuctionService;
import com.auction.client.service.BidService;
import com.auction.client.service.UserService;

/**
 * Lớp ngữ cảnh (Context) chứa các đối tượng toàn cục (giống Singleton)
 * dùng chung cho toàn bộ ứng dụng Client.
 */
public final class ClientContext {
    // thông tin kết nối đến Server
    private static final String SERVER_HOST = "maglev.proxy.rlwy.net";
    private static final int SERVER_PORT = 44658;

    // đối tượng duy nhất (singleton) quản lý phiên người dùng và kết nối mạng
    private static final SessionManager SESSION_MANAGER = new SessionManager();
    private static final SocketClient SOCKET_CLIENT = new SocketClient(SERVER_HOST, SERVER_PORT, SESSION_MANAGER);

    // các dịch vụ (services) toàn cục để tái sử dụng ở tất cả mọi nơi trong app
    private static final UserService USER_SERVICE = new UserService(SOCKET_CLIENT, SESSION_MANAGER);
    private static final AuctionService AUCTION_SERVICE = new AuctionService(SOCKET_CLIENT);
    private static final BidService BID_SERVICE = new BidService(SOCKET_CLIENT);

    // private constructor để ngăn việc tạo instance mới (không cho dùng từ khóa 'new')
    private ClientContext() {
    }

    // getter tĩnh để lấy instance của UserService
    public static UserService userService() {
        return USER_SERVICE;
    }

    // getter tĩnh để lấy instance của AuctionService
    public static AuctionService auctionService() {
        return AUCTION_SERVICE;
    }

    // getter tĩnh để lấy instance của BidService
    public static BidService bidService() {
        return BID_SERVICE;
    }
}