package com.auction.share.exceptions;

/** Tài nguyên bị trùng trong hệ thống. */
public class DuplicateResourceException extends AuctionSystemException {
  public DuplicateResourceException(String message) {
    super(message);
  }
}
