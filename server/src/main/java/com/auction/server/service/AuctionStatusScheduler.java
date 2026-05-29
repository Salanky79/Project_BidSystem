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
  private final ISchedulableAuctionService auctionService;
  private final List<AuctionLifecycleListener> listeners = new CopyOnWriteArrayList<>();
  private final long intervalMillis;
  private final AtomicBoolean running = new AtomicBoolean(false);

  public AuctionStatusScheduler(
      ISchedulableAuctionService auctionService,
      long intervalMillis) {
    this.auctionService = auctionService;
    this.intervalMillis = intervalMillis;
  }

  public void addListener(AuctionLifecycleListener listener) {
    if (listener != null) {
      listeners.add(listener);
    }
  }

  public void shutdown() {
    running.set(false);
  }

  @Override
  public void run() {
    if (!running.compareAndSet(false, true)) {
      throw new IllegalStateException("Scheduler already running");
    }
    try {
      while (running.get() && !Thread.currentThread().isInterrupted()) {
        try {
          List<String> finishedIds = auctionService.finishAuctionsAndGetIds();
          if (finishedIds != null && !finishedIds.isEmpty()) {
            listeners.forEach(l -> l.onAuctionsFinished(finishedIds));
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
      running.set(false);
    }
  }
}
