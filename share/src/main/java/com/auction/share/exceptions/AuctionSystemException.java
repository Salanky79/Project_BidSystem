package com.auction.share.exceptions;

/** Ngoại lệ gốc của module share. */
public class AuctionSystemException extends Exception {
  public AuctionSystemException(String message) {
    super(message);
  }
}