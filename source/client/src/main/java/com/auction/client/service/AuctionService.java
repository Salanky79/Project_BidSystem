package com.auction.client.service;

import com.auction.client.network.SocketClient;
import com.auction.share.DTO.*;
import com.auction.share.exceptions.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

/**
 * Dịch vụ xử lý các nghiệp vụ liên quan đến phiên đấu giá phía Client. Giao tiếp với Server thông
 * qua SocketClient.
 */
public class AuctionService {
  private final SocketClient socketClient;
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  public AuctionService(SocketClient socketClient) {
    this.socketClient = socketClient;
  }

  /** Tạo một phiên đấu giá mới kèm theo ảnh. */
  public void createAuction(
      String itemName,
      String description,
      String category,
      double startingPrice,
      LocalDateTime startTime,
      LocalDateTime endTime,
      byte[] imageBytes,
      String imageName,
      Consumer<Response<?>> onResponse)
      throws ValidationException {
    // Chỉ giữ business-rule validation
    if (startingPrice <= 0) {
      throw new ValidationException("Starting price must be greater than 0!");
    }
    if (endTime.isBefore(startTime)) {
      throw new ValidationException("End time must be after start time!");
    }

    String startTimeStr = startTime.format(FORMATTER);
    String endTimeStr = endTime.format(FORMATTER);

    CreateAuctionRequest request =
        new CreateAuctionRequest(
            null, itemName, description, category, startingPrice, startTimeStr, endTimeStr, imageBytes, imageName);
    socketClient.send(request, onResponse);
  }

  public void getAuctions(Consumer<Response<?>> onResponse) {
    socketClient.send(new ListAuctionRequest(), onResponse);
  }

  /** Lấy danh sách auction của seller đang đăng nhập, có thể lọc theo status. */
  public void getSellerAuctions(String status, Consumer<Response<?>> onResponse) {
    // sellerId = null, sellerOnly = true → server tự inject từ session
    socketClient.send(new ListAuctionRequest(status, null, true), onResponse);
  }

  public void cancelAuction(String auctionId, Consumer<Response<?>> onResponse) {
    CancelAuctionRequest request = new CancelAuctionRequest(auctionId);
    socketClient.send(request, onResponse);
  }

  public void setBidStep(String auctionId, double bidStep, Consumer<Response<?>> onResponse) {
    SetBidStepRequest request = new SetBidStepRequest(auctionId, bidStep, null);
    socketClient.send(request, onResponse);
  }

  public void registerAutoBid(
      String auctionId, double maxBid, double increment, Consumer<Response<?>> onResponse) {
    RegisterAutoBidRequest request = new RegisterAutoBidRequest(auctionId, maxBid, increment, null);
    socketClient.send(request, onResponse);
  }

  public void getAuctionDetail(String auctionId, Consumer<Response<?>> onResponse) {
    GetAuctionDetailRequest request = new GetAuctionDetailRequest(auctionId);
    socketClient.send(request, onResponse);
  }

  public AuctionPushRegistry createPushRegistry(String auctionId) {
    return new AuctionPushRegistry(socketClient, auctionId);
  }
}
