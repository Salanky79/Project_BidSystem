package com.auction.server.service;

import java.util.List;

public interface AuctionLifecycleCleaner {
    void onAuctionsFinished(List<String> auctionIds);
    default void onAuctionsStarted() {}
}
