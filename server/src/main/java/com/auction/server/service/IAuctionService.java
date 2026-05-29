package com.auction.server.service;

import com.auction.share.DTO.CreateAuctionRequest;
import com.auction.share.DTO.SetBidStepRequest;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;

import java.sql.SQLException;

public interface IAuctionService {
    Auction createAuction(CreateAuctionRequest request) throws SQLException, ValidationException;
    void cancelAuction(String auctionId, String requesterUserId) throws SQLException, ValidationException;
    boolean setBidStep(SetBidStepRequest request) throws SQLException, ValidationException;
}
