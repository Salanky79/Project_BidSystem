package com.auction.server.service;

import com.auction.server.network.AuctionSubscriptionRegistry;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.DTO.Response;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BroadcastService {
  public static final String BID_UPDATED = "BID_UPDATED";
  private static final Logger LOGGER = LoggerFactory.getLogger(BroadcastService.class);

  private final AuctionSubscriptionRegistry subscriptionRegistry;

  private final ExecutorService broadcastExecutor = new ThreadPoolExecutor(
      4, 16,
      60L, TimeUnit.SECONDS,
      new LinkedBlockingQueue<>(10_000),
      new ThreadPoolExecutor.CallerRunsPolicy()
  );

  public BroadcastService(AuctionSubscriptionRegistry subscriptionRegistry) {
    this.subscriptionRegistry = subscriptionRegistry;
  }

  // Gửi thông báo "có bid mới" đến tất cả client đang xem auction đó
  public void broadcastBidUpdate(BidUpdateEvent event) {
    try {
      Response<BidUpdateEvent> pushMessage = Response.success(BID_UPDATED, event);

      for (com.auction.server.network.ClientSession session :
          subscriptionRegistry.getSubscribers(event.getAuctionId())) {
        broadcastExecutor.submit(
            () -> {
              try {
                session.send(pushMessage);
              } catch (IOException e) {
                LOGGER.warn("Failed to push to session, removing: {}", e.getMessage());
                // xoá client đó khỏi tất cả auction subscription
                subscriptionRegistry.unsubscribeAll(session);
              }
            });
      }
    } catch (Exception e) {
      LOGGER.error("Broadcast failed entirely for auction {}", event.getAuctionId(), e);
    }
  }
  public void shutdown() {
    broadcastExecutor.shutdown();
    try {
      if (!broadcastExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
        broadcastExecutor.shutdownNow();
      }
    } catch (InterruptedException e) {
      broadcastExecutor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
