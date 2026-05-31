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
      String auctionId, double amount, double currentPrice, Consumer<Response<?>> onResponse)
      throws ValidationException {

    if (amount <= currentPrice) {
      throw new ValidationException(
          "Giá bid phải cao hơn giá hiện tại: " + String.format("%,.0f VND", currentPrice));
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
      throw new ValidationException("Gia toi da phai cao hon gia hien tai.");
    }
    if (increment <= 0) {
      throw new ValidationException("Buoc nhay auto-bid phai lon hon 0.");
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
