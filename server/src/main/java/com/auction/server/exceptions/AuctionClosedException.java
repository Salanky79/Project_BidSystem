package com.auction.server.exceptions;

public class AuctionClosedException extends RuntimeException {
    public AuctionClosedException(String message) {
        super(message);
    }

    public AuctionClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}

