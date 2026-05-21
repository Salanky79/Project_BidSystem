package com.auction.share.exceptions;

/**
 * Tài nguyên đã tồn tại.
 */
public class DuplicateResourceException extends AuctionSystemException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
