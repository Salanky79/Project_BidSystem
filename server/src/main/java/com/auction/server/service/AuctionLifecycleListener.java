package com.auction.server.service;

import java.util.List;

public interface AuctionLifecycleListener {
    void onAuctionsFinished(List<String> auctionIds);
}
