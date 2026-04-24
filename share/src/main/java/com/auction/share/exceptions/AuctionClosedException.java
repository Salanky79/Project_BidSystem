package com.auction.share.exceptions;

// Quăng khi phiên đấu giá đã kết thúc
public class AuctionClosedException extends RuntimeException {
    public AuctionClosedException(String message) {
        super(message);
    }

    public AuctionClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}