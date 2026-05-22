package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.model.AutoBidConfig;
import com.auction.share.DTO.CancelAutoBidRequest;
import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.DTO.RegisterAutoBidRequest;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoBidService {
    private static final int MAX_STEPS_PER_TRIGGER = 200;

    private final AutoBidRegistry registry;
    private final AuctionDAO auctionDAO;
    private final AuctionService auctionService;
    private final ExecutorService executor;
    private final Set<String> processingAuctions = ConcurrentHashMap.newKeySet();

    public AutoBidService(AutoBidRegistry registry, AuctionDAO auctionDAO, AuctionService auctionService) {
        this.registry = registry;
        this.auctionDAO = auctionDAO;
        this.auctionService = auctionService;
        executor = Executors.newSingleThreadExecutor();
    }

    public boolean register(RegisterAutoBidRequest request) throws SQLException, ValidationException {
        validateConfig(request.getMaxBid(), request.getIncrement());

        Auction auction = auctionDAO.findById(request.getAuctionId());
        if (!auctionService.isAuctionRunning(auction)) {
            throw new ValidationException("Auction is not running.");
        }
        if (request.getMaxBid() <= auction.getCurrentHighestBid()) {
            throw new ValidationException("Auto-bid max must be higher than current highest bid.");
        }

        auctionService.requireBidder(request.getBidderId());
        AutoBidConfig config = new AutoBidConfig(
                request.getBidderId(),
                request.getAuctionId(),
                request.getMaxBid(),
                request.getIncrement(),
                LocalDateTime.now()
        );
        registry.register(config);
        triggerAutoBid(request.getAuctionId(), request.getBidderId());
        return true;
    }

    public boolean cancel(CancelAutoBidRequest request) {
        return registry.cancel(request.getAuctionId(), request.getBidderId());
    }

    public void triggerAutoBid(String auctionId, String lastBidderId) {
        if (!processingAuctions.add(auctionId)) {
            return;
        }
        executor.submit(() -> {
            try {
                processAutoBid(auctionId, lastBidderId);
            } finally {
                processingAuctions.remove(auctionId);
            }
        });
    }

    public void processAutoBid(String auctionId, String lastBidderId) {
        String latestBidderId = lastBidderId;
        int steps = 0;

        while (steps++ < MAX_STEPS_PER_TRIGGER) {
            Auction auction;
            try {
                auction = auctionDAO.findById(auctionId);
            } catch (SQLException e) {
                return;
            }
            if (!auctionService.isAuctionRunning(auction)) {
                return;
            }

            List<AutoBidConfig> configs = registry.getConfigs(auctionId);
            if (configs.isEmpty()) {
                return;
            }

            AutoBidConfig candidate = pickCandidate(configs, latestBidderId, auction.getCurrentHighestBid());
            if (candidate == null) {
                return;
            }

            double nextAmount = auction.getCurrentHighestBid() + candidate.getIncrement();
            if (nextAmount > candidate.getMaxBid()) {
                registry.cancel(candidate.getAuctionId(), candidate.getBidderId());
                continue;
            }

            try {
                auctionService.placeBidInternal(
                        new PlaceBidRequest(candidate.getAuctionId(), candidate.getBidderId(), nextAmount),
                        false
                );
                latestBidderId = candidate.getBidderId();
            } catch (Exception e) {
                registry.cancel(candidate.getAuctionId(), candidate.getBidderId());
                return;
            }
        }
    }

    private static AutoBidConfig pickCandidate(List<AutoBidConfig> configs, String lastBidderId, double currentHighestBid) {
        return configs.stream()
                .filter(c -> !c.getBidderId().equals(lastBidderId))
                .filter(c -> c.getMaxBid() > currentHighestBid)
                .max(Comparator
                        .comparingDouble(AutoBidConfig::getMaxBid)
                        .thenComparing(AutoBidConfig::getRegisteredAt, Comparator.reverseOrder()))
                .orElse(null);
    }

    private static void validateConfig(double maxBid, double increment) throws ValidationException {
        if (maxBid <= 0) {
            throw new ValidationException("Auto-bid max must be greater than 0.");
        }
        if (increment <= 0) {
            throw new ValidationException("Auto-bid increment must be greater than 0.");
        }
    }
}
