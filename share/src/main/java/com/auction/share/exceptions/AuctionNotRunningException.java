package com.auction.share.exceptions;

// Quăng khi phiên đấu giá không ở trạng thái RUNNING
public class AuctionNotRunningException extends RuntimeException {
    public AuctionNotRunningException(String message) {
        super(message);
    }

    public AuctionNotRunningException(String message, Throwable cause) {
        super(message, cause);
    }
}