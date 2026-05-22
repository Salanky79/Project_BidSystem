package com.auction.share.exceptions;

/**
 * Không đủ số dư để thực hiện giao dịch.
 */
public class InsufficientFundsException extends AuctionSystemException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}