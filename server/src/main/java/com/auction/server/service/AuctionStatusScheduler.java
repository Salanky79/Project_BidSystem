package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Luồng chạy ngầm để quét và cập nhật trạng thái của các phiên đấu giá (Mở -> Đang chạy -> Kết thúc).
 */
public class AuctionStatusScheduler implements Runnable {
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
                throw new RuntimeException("Auction status scheduler DB error", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
