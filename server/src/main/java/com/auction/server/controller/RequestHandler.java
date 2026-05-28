package com.auction.server.controller;

import com.auction.server.network.AuctionSubscriptionRegistry;
import com.auction.server.network.ClientSession;
import com.auction.server.service.AuctionService;
import com.auction.server.service.AutoBidService;
import com.auction.server.service.UserService;
import com.auction.share.DTO.*;
import com.auction.share.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Bộ định tuyến (Router) nhận Request từ Client và điều hướng tới đúng Controller xử lý. */
public class RequestHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);
  private final UserController userController;
  private final AuctionController auctionController;
  private final AuctionSubscriptionRegistry subscriptionRegistry;

  // khởi tạo handler với các dependency nội bộ
  public RequestHandler(
      UserService userService,
      AuctionService auctionService,
      AutoBidService autoBidService,
      AuctionSubscriptionRegistry subscriptionRegistry) {
    // sử dụng chung các controller nhưng session sẽ được cấp vào từng request riêng
    this.userController = new UserController(userService);
    this.auctionController = new AuctionController(auctionService, autoBidService);
    // quản lý danh sách đăng ký theo dõi realtime
    this.subscriptionRegistry = subscriptionRegistry;
  }

  public Response<?> handle(Request request, ClientSession session) {
    Response<?> response;
    if (request.getAction() == null) {
      response = Response.fail("Action is required.");
      response.setRequestId(request.getRequestId());
      return response;
    }

    try {
      // switch expression phân tích hành động và chuyển tiếp yêu cầu
      response =
          switch (request.getAction()) {
            case Action.LOGIN -> userController.login((LoginRequest) request); // ép kiểu để lấy đúng dữ liệu
            case Action.REGISTER -> userController.register((RegisterRequest) request);
            case Action.UPDATE_PROFILE ->
                userController.updateProfile((UpdateProfileRequest) request);
            case Action.GET_PROFILE -> userController.getProfile((GetProfileRequest) request);
            case Action.CREATE_AUCTION ->
                auctionController.createAuction((CreateAuctionRequest) request);
            case Action.CANCEL_AUCTION ->
                auctionController.cancelAuction(
                    (CancelAuctionRequest) request,
                    session == null ? null : session.getUserId());
            case Action.PLACE_BID -> auctionController.placeBid((PlaceBidRequest) request);
            case Action.SET_AUTO_BID ->
                auctionController.registerAutoBid(
                    toRegisterAutoBidRequest((SetAutoBidRequest) request));
            case Action.REGISTER_AUTO_BID ->
                auctionController.registerAutoBid((RegisterAutoBidRequest) request);
            case Action.CANCEL_AUTO_BID ->
                auctionController.cancelAutoBid((CancelAutoBidRequest) request);
            case Action.GET_AUCTION_DETAIL ->
                auctionController.getAuctionDetail((GetAuctionDetailRequest) request);
            case Action.LIST_AUCTIONS ->
                auctionController.listAuctions((ListAuctionRequest) request);
            case Action.SET_BID_STEP -> auctionController.setBidStep((SetBidStepRequest) request);
            case Action.EXTEND_END_TIME ->
                auctionController.extendEndTime((ExtendEndTimeRequest) request);
            case Action.UNSUBSCRIBE_AUCTION ->
                handleUnsubscribe((UnsubscribeAuctionRequest) request, session);
            default -> Response.fail("Unsupported action: " + request.getAction());
          };
    } catch (ClassCastException e) {
      response = Response.fail("Request type does not match action.");
    } catch (ValidationException e) {
      response = Response.fail(e.getMessage());
    } catch (Exception e) {
      LOGGER.error("Unhandled request processing error for action={}", request.getAction(), e);
      response = Response.fail("An internal error occurred.");
    }
    response.setRequestId(request.getRequestId());
    return response;
  }

  private Response<Boolean> handleUnsubscribe(
      UnsubscribeAuctionRequest request, ClientSession session) {
    if (session == null) {
      return Response.fail("Session is required.");
    }

    String auctionId = request == null ? null : request.getAuctionId();
    if (auctionId == null || auctionId.isBlank()) {
        // tắt app hoặc đăng xuất
      subscriptionRegistry.unsubscribeAll(session);
      return Response.success("Unsubscribed from all auctions.", true);
    }

    subscriptionRegistry.unsubscribe(auctionId, session);
    return Response.success("Unsubscribed from auction: " + auctionId, true);
  }

  // gọi ở trên handle
  private RegisterAutoBidRequest toRegisterAutoBidRequest(SetAutoBidRequest request) {
    return new RegisterAutoBidRequest(
        request.getAuctionId(), request.getMaxBid(), request.getIncrement(), request.getBidderId());
  }
}
