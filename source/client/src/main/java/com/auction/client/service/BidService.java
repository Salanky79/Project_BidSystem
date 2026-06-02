package com.auction.client.service;

import com.auction.client.network.SocketClient;
import com.auction.share.DTO.CancelAutoBidRequest;
import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.DTO.RegisterAutoBidRequest;
import com.auction.share.DTO.Response;
import com.auction.share.exceptions.ValidationException;
import java.util.function.Consumer;

public class BidService {
  private final SocketClient socketClient;

  public BidService(SocketClient socketClient) {
    this.socketClient = socketClient;
  }

  public void placeBid(
      String auctionId, double amount, double minBid, Consumer<Response<?>> onResponse)
      throws ValidationException {

    if (amount < minBid) {
      throw new ValidationException(
          "Bid amount must be greater than or equal to minimum bid: " + String.format("%,.0f VND", minBid));
    }
    PlaceBidRequest request = new PlaceBidRequest(auctionId, null, amount);
    socketClient.send(request, onResponse);
  }

  public void registerAutoBid(
      String auctionId,
      double maxBid,
      double increment,
      double currentPrice,
      Consumer<Response<?>> onResponse)
      throws ValidationException {

    if (maxBid <= currentPrice) {
      throw new ValidationException("Max bid must be higher than current price.");
    }
    if (increment <= 0) {
      throw new ValidationException("Auto-bid increment must be greater than 0.");
    }

    RegisterAutoBidRequest request = new RegisterAutoBidRequest(auctionId, maxBid, increment, null);
    socketClient.send(request, onResponse);
  }

  public void setAutoBid(
      String auctionId,
      double maxBid,
      double increment,
      double currentPrice,
      Consumer<Response<?>> onResponse)
      throws ValidationException {
    registerAutoBid(auctionId, maxBid, increment, currentPrice, onResponse);
  }

  public void cancelAutoBid(String auctionId, Consumer<Response<?>> onResponse)
      throws ValidationException {
    if (auctionId == null || auctionId.isBlank()) {
      throw new ValidationException("Auction ID is required.");
    }

    CancelAutoBidRequest request = new CancelAutoBidRequest(auctionId, null);
    socketClient.send(request, onResponse);
  }
}
