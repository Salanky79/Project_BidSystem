package com.auction.share.exceptions;

/**
 * Thrown when a bid is rejected due to a concurrent price change or being outbid.
 */
public class ConcurrentBidException extends ValidationException {
    public ConcurrentBidException(String message) {
        super(message);
    }
}
