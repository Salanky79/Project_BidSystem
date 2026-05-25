package com.auction.share.exceptions;

/** Phiên đấu giá đã đóng. */
public class AuctionClosedException extends AuctionSystemException {
  public AuctionClosedException(String message) {
    super(message);
  }
}