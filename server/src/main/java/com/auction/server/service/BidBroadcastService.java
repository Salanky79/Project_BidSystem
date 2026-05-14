package com.auction.server.service;

import com.auction.server.network.AuctionSubscriptionRegistry;
import com.auction.server.network.ClientSession;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.DTO.Response;

import java.io.IOException;

public class BidBroadcastService {
    public static final String BID_UPDATED = "BID_UPDATED";

    private final AuctionSubscriptionRegistry subscriptionRegistry;

    public BidBroadcastService(AuctionSubscriptionRegistry subscriptionRegistry) {
        this.subscriptionRegistry = subscriptionRegistry;
    }

    public void broadcastBidUpdate(BidUpdateEvent event) {
        Response<BidUpdateEvent> pushMessage = Response.success(BID_UPDATED, event);

        for (ClientSession session : subscriptionRegistry.getSubscribers(event.getAuctionId())) {
            try {
                session.send(pushMessage);
            } catch (IOException ignored) {
                subscriptionRegistry.removeSession(session);
            }
        }
    }
}
