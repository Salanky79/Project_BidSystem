package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Luồng chạy ngầm để quét và cập nhật trạng thái của các phiên đấu giá (Mở -> Đang chạy -> Kết
 * thúc).
 */
public class AuctionStatusScheduler implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuctionStatusScheduler.class);
  private final AuctionDAO auctionDAO;
  private final long intervalMillis;
  private final AtomicBoolean running = new AtomicBoolean(true);

  public AuctionStatusScheduler(AuctionDAO auctionDAO, long intervalMillis) {
    this.auctionDAO = auctionDAO;
    this.intervalMillis = intervalMillis;
  }

  public void shutdown() {
    running.set(false);
  }

  @Override
  public void run() {
    while (running.get() && !Thread.currentThread().isInterrupted()) {
      try {
        // mỗi chu kỳ (interval) kiểm tra trạng thái các phiên đấu giá
        auctionDAO.markOpenAuctionsAsRunning();
        auctionDAO.markRunningAuctionsAsFinished();
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
  }
}
