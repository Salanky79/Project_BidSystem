package com.auction.server.service;

import com.auction.server.network.AuctionSubscriptionRegistry;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.DTO.Response;
import java.io.IOException;
import java.util.concurrent.Executors;

public class BidBroadcastService {
  public static final String BID_UPDATED = "BID_UPDATED";

  private final AuctionSubscriptionRegistry subscriptionRegistry;

  private final java.util.concurrent.ExecutorService broadcastExecutor = Executors.newCachedThreadPool();
  // gửi message song song

  public BidBroadcastService(AuctionSubscriptionRegistry subscriptionRegistry) {
    this.subscriptionRegistry = subscriptionRegistry;
  }

  // Gửi thông báo "có bid mới" đến tất cả client đang xem auction đó
  public void broadcastBidUpdate(BidUpdateEvent event) {
    Response<BidUpdateEvent> pushMessage = Response.success(BID_UPDATED, event);

    for (com.auction.server.network.ClientSession session :
        subscriptionRegistry.getSubscribers(event.getAuctionId())) {
      broadcastExecutor.submit(
          () -> {
            try {
              session.send(pushMessage);
            } catch (IOException ignored) {
              // xoá client đó khỏi tất cả auction subscription
                // client có thể đã disconnect
              subscriptionRegistry.unsubscribeAll(session);
            }
          });
    }
  }
}
