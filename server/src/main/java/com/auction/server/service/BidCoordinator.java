package com.auction.server.service;

import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.exceptions.ValidationException;

import java.sql.SQLException;

public class BidCoordinator {
    private final BidService bidService;
    private final AutoBidService autoBidService;

    public BidCoordinator(BidService bidService, AutoBidService autoBidService) {
        this.bidService = bidService;
        this.autoBidService = autoBidService;
    }

    public boolean placeBidAndTriggerAuto(PlaceBidRequest request) throws SQLException, ValidationException {
        boolean result = bidService.placeBid(request);
        autoBidService.triggerAutoBid(request.getAuctionId(), request.getBidderId());
        return result;
    }
}
