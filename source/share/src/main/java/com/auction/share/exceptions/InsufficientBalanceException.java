package com.auction.share.exceptions;

/**
 * Thrown when a user does not have enough balance to perform an action.
 */
public class InsufficientBalanceException extends ValidationException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
