package com.auction.share.exceptions;

// Quăng khi phiên đấu giá chưa bắt đầu
public class AuctionNotStartedException extends RuntimeException {
    public AuctionNotStartedException(String message) {
        super(message);
    }

    public AuctionNotStartedException(String message, Throwable cause) {
        super(message, cause);
    }
}