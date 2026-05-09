package com.auction.share.exceptions;

/**
 * Ném ra khi tài nguyên đã tồn tại (username trùng, email trùng, v.v).
 */
public class DuplicateResourceException extends AuctionSystemException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
