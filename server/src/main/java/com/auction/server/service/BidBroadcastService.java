package com.auction.server.service;

import com.auction.server.network.AuctionSubscriptionRegistry;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.DTO.Response;
import java.io.IOException;

public class BidBroadcastService {
  public static final String BID_UPDATED = "BID_UPDATED";

  private final AuctionSubscriptionRegistry subscriptionRegistry;

  public BidBroadcastService(AuctionSubscriptionRegistry subscriptionRegistry) {
    this.subscriptionRegistry = subscriptionRegistry;
  }

  // Gửi thông báo "có bid mới" đến tất cả client đang xem auction đó
  public void broadcastBidUpdate(BidUpdateEvent event) {
    Response<BidUpdateEvent> pushMessage = Response.success(BID_UPDATED, event);

    subscriptionRegistry.getSubscribers(event.getAuctionId()).parallelStream()
        .forEach(
            session -> {
              try {
                session.send(pushMessage);
              } catch (IOException ignored) {
                // xoá client đó khỏi tất cả auction subscription
                subscriptionRegistry.unsubscribeAll(session);
              }
            });
  }
}
