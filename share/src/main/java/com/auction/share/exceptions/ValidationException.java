package com.auction.share.exceptions;

/**
 * Ném ra khi dữ liệu đầu vào không hợp lệ (thiếu field, sai format, v.v).
 */
public class ValidationException extends AuctionSystemException {
    public ValidationException(String message) {
        super(message);
    }
}
