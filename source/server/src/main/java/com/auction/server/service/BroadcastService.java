package com.auction.server.service;

import com.auction.server.network.ClientSession;
import com.auction.share.DTO.AutoBidCancelledEvent;
import com.auction.share.DTO.BidStepUpdateEvent;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.DTO.Response;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BroadcastService implements AuctionLifecycleCleaner {
  public static final String BID_UPDATED = "BID_UPDATED";
  public static final String AUCTION_FINISHED = "AUCTION_FINISHED";
  public static final String AUCTION_CANCELLED = "AUCTION_CANCELLED";
  public static final String BID_STEP_UPDATED = "BID_STEP_UPDATED";
  public static final String AUCTION_STARTED = "AUCTION_STARTED";
  public static final String AUTO_BID_CANCELLED = "AUTO_BID_CANCELLED";
  private static final Logger LOGGER = LoggerFactory.getLogger(BroadcastService.class);

  private final AuctionSubscriptionRegistry subscriptionRegistry;

  private final ExecutorService broadcastExecutor = Executors.newFixedThreadPool(2);

  public BroadcastService(AuctionSubscriptionRegistry subscriptionRegistry) {
    this.subscriptionRegistry = subscriptionRegistry;
  }

  // Gửi thông báo "có bid mới" đến tất cả client đang xem auction đó
  public void broadcastBidUpdate(BidUpdateEvent event) {
    try {
      Response<BidUpdateEvent> pushMessage = Response.success(BID_UPDATED, event);

      for (ClientSession session : subscriptionRegistry.getAllSessions()) {
        broadcastExecutor.submit(
            () -> {
              try {
                session.send(pushMessage);
              } catch (IOException e) {
                LOGGER.warn("Failed to push to session, removing: {}", e.getMessage());
                subscriptionRegistry.removeSession(session);
              }
            });
      }
    } catch (Exception e) {
      LOGGER.error("Broadcast failed entirely for auction {}", event.getAuctionId(), e);
    }
  }

  public void broadcastAuctionCancelled(String auctionId) {
    try {
      Response<String> pushMessage = Response.success(AUCTION_CANCELLED, auctionId);
      for (ClientSession session : subscriptionRegistry.getAllSessions()) {
        broadcastExecutor.submit(
            () -> {
              try {
                session.send(pushMessage);
              } catch (IOException e) {
                LOGGER.warn("Failed to push cancel to session, removing: {}", e.getMessage());
                subscriptionRegistry.removeSession(session);
              }
            });
      }
    } catch (Exception e) {
      LOGGER.error("Broadcast cancel failed entirely for auction {}", auctionId, e);
    }
  }

  public void broadcastAuctionFinished(String auctionId) {
    try {
      Response<String> pushMessage = Response.success(AUCTION_FINISHED, auctionId);
      for (ClientSession session : subscriptionRegistry.getAllSessions()) {
        broadcastExecutor.submit(
            () -> {
              try {
                session.send(pushMessage);
              } catch (IOException e) {
                LOGGER.warn("Failed to push finish to session, removing: {}", e.getMessage());
                subscriptionRegistry.removeSession(session);
              }
            });
      }
    } catch (Exception e) {
      LOGGER.error("Broadcast finish failed entirely for auction {}", auctionId, e);
    }
  }

  public void broadcastAuctionStarted() {
    try {
      Response<String> pushMessage = Response.success(AUCTION_STARTED, "New auctions started");
      for (ClientSession session : subscriptionRegistry.getAllSessions()) {
        broadcastExecutor.submit(
            () -> {
              try {
                session.send(pushMessage);
              } catch (IOException e) {
                LOGGER.warn("Failed to push started to session, removing: {}", e.getMessage());
                subscriptionRegistry.removeSession(session);
              }
            });
      }
    } catch (Exception e) {
      LOGGER.error("Broadcast started failed entirely", e);
    }
  }

  public void broadcastBidStepUpdated(String auctionId, double newBidStep) {
    try {
      BidStepUpdateEvent event = new BidStepUpdateEvent(auctionId, newBidStep);
      Response<BidStepUpdateEvent> pushMessage = Response.success(BID_STEP_UPDATED, event);
      for (ClientSession session : subscriptionRegistry.getAllSessions()) {
        broadcastExecutor.submit(
            () -> {
              try {
                session.send(pushMessage);
              } catch (IOException e) {
                LOGGER.warn("Failed to push bid step update to session, removing: {}", e.getMessage());
                subscriptionRegistry.removeSession(session);
              }
            });
      }
    } catch (Exception e) {
      LOGGER.error("Broadcast bid step update failed entirely for auction {}", auctionId, e);
    }
  }

  public void broadcastAutoBidCancelled(String userId, String auctionId, String reason) {
    try {
      AutoBidCancelledEvent event = new AutoBidCancelledEvent(auctionId, reason);
      Response<AutoBidCancelledEvent> pushMessage = Response.success(AUTO_BID_CANCELLED, event);
      for (ClientSession session : subscriptionRegistry.getAllSessions()) {
        if (userId.equals(session.getUserId())) {
          broadcastExecutor.submit(
              () -> {
                try {
                  session.send(pushMessage);
                } catch (IOException e) {
                  LOGGER.warn("Failed to push auto-bid cancel to session: {}", e.getMessage());
                  subscriptionRegistry.removeSession(session);
                }
              });
        }
      }
    } catch (Exception e) {
      LOGGER.error("Broadcast auto-bid cancel failed for user {}", userId, e);
    }
  }

  @Override
  public void onAuctionsFinished(List<String> auctionIds) {
    if (auctionIds == null) return;
    for (String id : auctionIds) {
      broadcastAuctionFinished(id);
    }
  }

  @Override
  public void onAuctionsStarted() {
    broadcastAuctionStarted();
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
