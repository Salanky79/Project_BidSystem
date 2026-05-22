package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

public class AuctionStatusScheduler implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(AuctionStatusScheduler.class);
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
        log.info("AuctionStatusScheduler started with intervalMillis={}", intervalMillis);
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                // cu moi ms kiem tra xem auction nao con chay hay da xog
                int openedToRunning = auctionDAO.markOpenAuctionsAsRunning();
                int runningToFinished = auctionDAO.markRunningAuctionsAsFinished();
                if (openedToRunning > 0 || runningToFinished > 0) {
                    log.info("AuctionStatusScheduler tick: OPEN->RUNNING={}, RUNNING->FINISHED={}",
                            openedToRunning, runningToFinished);
                }
                Thread.sleep(intervalMillis);
            } catch (SQLException e) {
                log.error("AuctionStatusScheduler database error", e);
                throw new RuntimeException("Auction status scheduler DB error", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("AuctionStatusScheduler interrupted. Stopping scheduler loop.");
                break;
            }
        }
        log.info("AuctionStatusScheduler stopped.");
    }
}
