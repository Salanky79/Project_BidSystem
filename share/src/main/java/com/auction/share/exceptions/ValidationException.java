package com.auction.share.exceptions;

/** Dữ liệu đầu vào không hợp lệ. */
public class ValidationException extends AuctionSystemException {
  public ValidationException(String message) {
    super(message);
  }
}
