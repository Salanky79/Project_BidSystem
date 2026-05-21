package com.auction.server.service;

import com.auction.server.network.AuctionSubscriptionRegistry;
import com.auction.server.network.ClientSession;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.DTO.Response;

import java.io.IOException;

/**
 * Phát thông báo bid realtime tới các client đang theo dõi.
 */
public class BidBroadcastService {
    public static final String BID_UPDATED = "BID_UPDATED";

    private final AuctionSubscriptionRegistry subscriptionRegistry;

    public BidBroadcastService(AuctionSubscriptionRegistry subscriptionRegistry) {
        this.subscriptionRegistry = subscriptionRegistry;
    }

    // Gửi thông báo có bid mới tới tất cả client đang xem auction.
    public void broadcastBidUpdate(BidUpdateEvent event) {
        Response<BidUpdateEvent> pushMessage = Response.success(BID_UPDATED, event);

        for (ClientSession session : subscriptionRegistry.getSubscribers(event.getAuctionId())) {
            try {
                session.send(pushMessage);
            } catch (IOException ignored) {
                subscriptionRegistry.unsubcribe(session);
            }
        }
    }
}
