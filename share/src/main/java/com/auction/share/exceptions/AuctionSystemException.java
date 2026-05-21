package com.auction.share.exceptions;

/**
 * Ngoại lệ gốc của hệ thống đấu giá.
 */
public class AuctionSystemException extends Exception {
    public AuctionSystemException(String message) {
        super(message);
    }
}