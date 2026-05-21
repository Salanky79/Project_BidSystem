package com.auction.share.exceptions;

/**
 * Không tìm thấy phiên đấu giá.
 */
public class AuctionNotFoundException extends AuctionSystemException {
    public AuctionNotFoundException(String message) {
        super(message);
    }
}