package com.auction.share.exceptions;

/**
 * Giá đặt không hợp lệ.
 */
public class InvalidBidException extends AuctionSystemException {
    public InvalidBidException(String message) {
        super(message);
    }
}