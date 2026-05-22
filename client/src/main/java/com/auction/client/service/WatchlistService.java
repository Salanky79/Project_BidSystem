package com.auction.client.service;

import java.util.HashSet;
import java.util.Set;

/**
 * Dịch vụ quản lý danh sách các phiên đấu giá đang theo dõi (Watchlist) phía Client.
 * Áp dụng pattern Singleton để giữ trạng thái duy nhất.
 */
public class WatchlistService {
    private static final WatchlistService instance = new WatchlistService();
    // sử dụng Set để tránh lưu trùng lặp ID
    private final Set<String> followedAuctionIds;

    private WatchlistService() {
        followedAuctionIds = new HashSet<>();
    }

    public static WatchlistService getInstance() {
        return instance;
    }

    public boolean isFollowed(String auctionId) {
        return followedAuctionIds.contains(auctionId);
    }

    public void add(String auctionId) {
        followedAuctionIds.add(auctionId);
    }

    public void remove(String auctionId) {
        followedAuctionIds.remove(auctionId);
    }

    public void toggle(String auctionId) {
        if (isFollowed(auctionId)) {
            remove(auctionId);
        } else {
            add(auctionId);
        }
    }
}
