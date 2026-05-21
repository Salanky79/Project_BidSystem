package com.auction.share.exceptions;

/**
 * Ném ra khi giá đặt không hợp lệ (ví dụ: thấp hơn giá hiện tại).
 */
public class InvalidBidException extends AuctionSystemException {
    public InvalidBidException(String message) {
        super(message);
    }
}