package com.auction.server.service;

import com.auction.server.model.AutoBidConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutoBidRegistry {
    private final Map<String, ConcurrentHashMap<String, AutoBidConfig>> configsByAuction = new ConcurrentHashMap<>();

    public void register(AutoBidConfig config) {
        configsByAuction
                .computeIfAbsent(config.getAuctionId(), ignored -> new ConcurrentHashMap<>())
                .put(config.getBidderId(), config);
    }

    public boolean cancel(String auctionId, String bidderId) {
        ConcurrentHashMap<String, AutoBidConfig> byBidder = configsByAuction.get(auctionId);
        if (byBidder == null) {
            return false;
        }
        AutoBidConfig removed = byBidder.remove(bidderId);
        if (byBidder.isEmpty()) {
            configsByAuction.remove(auctionId, byBidder);
        }
        return removed != null;
    }

    public void cancelAll(String bidderId) {
        for (Map.Entry<String, ConcurrentHashMap<String, AutoBidConfig>> entry : configsByAuction.entrySet()) {
            ConcurrentHashMap<String, AutoBidConfig> byBidder = entry.getValue();
            byBidder.remove(bidderId);
            if (byBidder.isEmpty()) {
                configsByAuction.remove(entry.getKey(), byBidder);
            }
        }
    }

    public List<AutoBidConfig> getConfigs(String auctionId) {
        ConcurrentHashMap<String, AutoBidConfig> byBidder = configsByAuction.get(auctionId);
        if (byBidder == null || byBidder.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(byBidder.values());
    }
}
