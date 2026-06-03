package com.auction.server.controller;

import com.auction.server.mapper.UserMapper;
import com.auction.server.service.AuctionSubscriptionRegistry;
import com.auction.server.network.ClientSession;
import com.auction.server.service.AuctionService;
import com.auction.server.service.AutoBidService;
import com.auction.server.service.UserService;
import com.auction.server.service.BidService;
import com.auction.server.service.AuctionQueryService;
import com.auction.share.DTO.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/** Bộ điều phối (Dispatcher) nhận Request từ Client và điều hướng tới đúng ActionProcessor xử lý. */
public class RequestDispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestDispatcher.class);
    private final Map<String, ActionProcessor<Request>> processors = new HashMap<>();
    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
    private final AuctionSubscriptionRegistry subscriptionRegistry;
    private final UserController userController;
    private final AuctionController auctionController;

    public RequestDispatcher(UserController userController, AuctionController auctionController, AuctionSubscriptionRegistry subscriptionRegistry) {
        
        this.userController = userController;
        this.auctionController = auctionController;
        this.subscriptionRegistry = subscriptionRegistry;

        // Đăng ký các processors
        register(Action.LOGIN, req -> {
            Response<UserDTO> res = userController.login((LoginRequest) req);
            if (res.isSuccess() && res.getData() != null) {
                res.setAuthenticatedUserId(res.getData().getId());
            }
            return res;
        });
        register(Action.REGISTER, req -> userController.register((RegisterRequest) req));
        register(Action.UPDATE_PROFILE, req -> userController.updateProfile((UpdateProfileRequest) req));
        register(Action.GET_PROFILE, req -> userController.getProfile((GetProfileRequest) req));
        register(Action.DEPOSIT, req -> userController.deposit((DepositRequest) req));

        register(Action.CREATE_AUCTION, req -> auctionController.createAuction((CreateAuctionRequest) req));
        register(Action.CANCEL_AUCTION, req -> auctionController.cancelAuction((CancelAuctionRequest) req));
        register(Action.PLACE_BID, req -> auctionController.placeBid((PlaceBidRequest) req));
        
        register(Action.REGISTER_AUTO_BID, req -> auctionController.registerAutoBid((RegisterAutoBidRequest) req));
        register(Action.CANCEL_AUTO_BID, req -> auctionController.cancelAutoBid((CancelAutoBidRequest) req));
        
        register(Action.GET_AUCTION_DETAIL, req -> auctionController.getAuctionDetail((GetAuctionDetailRequest) req));
        register(Action.LIST_AUCTIONS, req -> auctionController.listAuctions((ListAuctionRequest) req));
        register(Action.SET_BID_STEP, req -> auctionController.setBidStep((SetBidStepRequest) req));
        
    }

    private <T extends Request> void register(String action, ActionProcessor<T> processor) {
        processors.put(action, (ActionProcessor<Request>) processor);
    }

    public Response<?> handle(Request request, ClientSession session) {
        Response<?> response;
        if (request.getAction() == null) {
            response = Response.fail("Action is required.");
            response.setRequestId(request.getRequestId());
            return response;
        }

        if (Action.UNSUBSCRIBE_AUCTION.equals(request.getAction())) {
            response = handleUnsubscribe((UnsubscribeAuctionRequest) request, session);
            response.setRequestId(request.getRequestId());
            return response;
        }

        ActionProcessor<Request> processor = processors.get(request.getAction());
        if (processor == null) {
            response = Response.fail("Unsupported action: " + request.getAction());
            response.setRequestId(request.getRequestId());
            return response;
        }

        try {
            response = processor.process(request);
        } catch (Exception e) {
            response = exceptionHandler.handle(e, request.getAction());
        }

        response.setRequestId(request.getRequestId());
        return response;
    }

    private Response<Boolean> handleUnsubscribe(UnsubscribeAuctionRequest request, ClientSession session) {
        if (session == null) {
            return Response.fail("Session is required.");
        }

        String auctionId = request == null ? null : request.getAuctionId();
        if (auctionId == null || auctionId.isEmpty()) {
            return Response.success("Unsubscribed from all auctions.", true);
        }

        return Response.success("Unsubscribed from auction: " + auctionId, true);
    }
}
