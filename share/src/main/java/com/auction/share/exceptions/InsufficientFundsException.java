package com.auction.share.exceptions;

public class InsufficientFundsException extends AuctionSystemException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}