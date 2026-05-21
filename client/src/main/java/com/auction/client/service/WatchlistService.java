package com.auction.client.service;

import java.util.HashSet;
import java.util.Set;

/**
 * Quản lý danh sách auction đang theo dõi trên client.
 */
public class WatchlistService {
    private static final WatchlistService instance = new WatchlistService();
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
