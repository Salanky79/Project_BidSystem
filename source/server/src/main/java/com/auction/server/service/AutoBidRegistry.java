package com.auction.server.service;

import com.auction.server.util.AutoBidConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Bộ nhớ tạm (In-memory Registry) lưu trữ danh sách cài đặt Auto-bid của tất cả user. */
public class AutoBidRegistry implements AuctionLifecycleCleaner {
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

  public void clearAuction(String auctionId) {
    if (auctionId == null || auctionId.isBlank()) {
      return;
    }
    configsByAuction.remove(auctionId);
  }

  public List<AutoBidConfig> getConfigs(String auctionId) {
    ConcurrentHashMap<String, AutoBidConfig> byBidder = configsByAuction.get(auctionId);
    if (byBidder == null || byBidder.isEmpty()) {
      return List.of();
    }
    return new ArrayList<>(byBidder.values());
  }

  @Override
  public void onAuctionsFinished(List<String> auctionIds) {
    if (auctionIds == null) return;
    for (String id : auctionIds) {
      clearAuction(id);
    }
  }
}
