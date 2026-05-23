package com.auction.share.exceptions;

/** Lỗi kết nối mạng giữa client và server. */
public class NetworkConnectionException extends AuctionSystemException {
  public NetworkConnectionException(String message) {
    super(message);
  }
}