package com.auction.server.service;

import com.auction.share.DTO.CancelAutoBidRequest;
import com.auction.share.DTO.RegisterAutoBidRequest;
import com.auction.share.exceptions.ValidationException;

import java.sql.SQLException;

public interface IAutoBidService {
    boolean register(RegisterAutoBidRequest request) throws SQLException, ValidationException;
    boolean cancel(CancelAutoBidRequest request);
    void triggerAutoBid(String auctionId, String lastBidderId);
}
