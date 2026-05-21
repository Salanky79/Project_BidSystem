package com.auction.share.exceptions;

/**
 * Ném ra khi người dùng cố gắng thao tác trên một phiên đấu giá đã đóng.
 */
public class AuctionClosedException extends AuctionSystemException {
    public AuctionClosedException(String message) {
        super(message);
    }
}