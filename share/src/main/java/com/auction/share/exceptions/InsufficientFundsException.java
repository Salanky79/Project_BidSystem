package com.auction.share.exceptions;

/**
 * Số dư không đủ để thực hiện giao dịch.
 */
public class InsufficientFundsException extends AuctionSystemException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}