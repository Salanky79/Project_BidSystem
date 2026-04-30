package com.auction.server.exceptions;

import com.auction.share.exceptions.AuctionSystemException;

public class DataValidationException extends AuctionSystemException {
    public DataValidationException(String message) {
        super(message);
    }
}