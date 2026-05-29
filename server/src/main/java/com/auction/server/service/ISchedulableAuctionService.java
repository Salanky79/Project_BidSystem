package com.auction.server.service;

import java.sql.SQLException;
import java.util.List;

public interface ISchedulableAuctionService extends IAuctionService {
    List<String> finishAuctionsAndGetIds() throws SQLException;
}
