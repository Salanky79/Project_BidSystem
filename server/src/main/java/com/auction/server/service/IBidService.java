package com.auction.server.service;

import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.exceptions.ValidationException;

import java.sql.SQLException;

public interface IBidService {
    boolean placeBid(PlaceBidRequest req, boolean triggerAuto) throws SQLException, ValidationException;
    void setAutoBidService(IAutoBidService autoBidService);
}
