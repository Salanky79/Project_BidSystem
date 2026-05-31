package com.auction.server.service;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Luồng chạy ngầm để quét và cập nhật trạng thái của các phiên đấu giá (Mở -> Đang chạy -> Kết
 * thúc).
 */
public class AuctionStatusScheduler implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuctionStatusScheduler.class);
  private final IAuctionService auctionService;
  private final List<AuctionLifecycleCleaner> listeners = new CopyOnWriteArrayList<>();
  private final long intervalMillis;
  private volatile boolean running = false;

  public AuctionStatusScheduler(
      IAuctionService auctionService,
      long intervalMillis) {
    this.auctionService = auctionService;
    this.intervalMillis = intervalMillis;
  }

  public void addListener(AuctionLifecycleCleaner listener) {
    if (listener != null) {
      listeners.add(listener);
    }
  }

  public void shutdown() {
    this.running = false;
  }

  @Override
  public void run() {
    if (running) {
      throw new IllegalStateException("Scheduler already running");
    }
    try {
      running = true;
      while (running && !Thread.currentThread().isInterrupted()) {
        try {
          List<String> finishedIds = auctionService.updateAuctionStatusesAndGetFinishedIds();
          if (!finishedIds.isEmpty()) {
            notifyListeners(finishedIds);
          }
          Thread.sleep(intervalMillis);
        } catch (SQLException e) {
          LOGGER.error("Scheduler DB error, retry in {}ms", intervalMillis, e);
          try {
            Thread.sleep(intervalMillis);
          } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            break;
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    } finally {
      this.running = false;
    }
  }

  private void notifyListeners(List<String> finishedIds) {
    for (AuctionLifecycleCleaner listener : listeners) {
      try {
        listener.onAuctionsFinished(finishedIds);
      } catch (Exception e) {
        LOGGER.error("Listener failed", e);
      }
    }
  }
}
