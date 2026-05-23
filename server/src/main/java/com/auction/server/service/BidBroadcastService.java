package com.auction.server.service;

import com.auction.server.network.AuctionSubscriptionRegistry;
import com.auction.server.network.ClientSession;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.DTO.Response;

import java.io.IOException;

/**
 * Dịch vụ phát sóng dữ liệu (Broadcast) tới các Client đang theo dõi phiên đấu giá.
 */
public class BidBroadcastService {
    public static final String BID_UPDATED = "BID_UPDATED";

    private final AuctionSubscriptionRegistry subscriptionRegistry;

    public BidBroadcastService(AuctionSubscriptionRegistry subscriptionRegistry) {
        this.subscriptionRegistry = subscriptionRegistry;
    }

    // phát sóng thông báo (broadcast) có lượt đặt giá mới tới tất cả người theo dõi
    public void broadcastBidUpdate(BidUpdateEvent event) {
        Response<BidUpdateEvent> pushMessage = Response.success(BID_UPDATED, event);

        subscriptionRegistry.getSubscribers(event.getAuctionId())
                .parallelStream()
                .forEach(session -> {
                    try {
                        session.send(pushMessage);
                    } catch (IOException ignored) {
                        // hủy đăng ký nếu gửi thất bại (client bị ngắt kết nối)
                        subscriptionRegistry.unsubscribeAll(session);
                    }
                });
    }
}
