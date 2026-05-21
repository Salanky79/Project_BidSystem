package com.auction.share.exceptions;

/**
 * Lỗi kết nối mạng.
 */
public class NetworkConnectionException extends AuctionSystemException {
    public NetworkConnectionException(String message) {
        super(message);
    }
}