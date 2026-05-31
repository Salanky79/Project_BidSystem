package com.auction.server.controller;

import com.auction.server.mapper.UserMapper;
import com.auction.server.network.AuctionSubscriptionRegistry;
import com.auction.server.network.ClientSession;
import com.auction.server.service.IAuctionService;
import com.auction.server.service.IAutoBidService;
import com.auction.server.service.IUserService;
import com.auction.server.service.BidCoordinator;
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

    public RequestDispatcher(
            IUserService userService,
            IAuctionService auctionService,
            IAutoBidService autoBidService,
            BidCoordinator bidCoordinator,
            AuctionQueryService auctionQueryService,
            AuctionSubscriptionRegistry subscriptionRegistry,
            com.auction.server.service.BroadcastService bidBroadcastService) {
        
        UserController userController = new UserController(userService, new UserMapper());
        AuctionController auctionController = new AuctionController(auctionService, autoBidService, bidCoordinator, auctionQueryService, bidBroadcastService);
        this.subscriptionRegistry = subscriptionRegistry;

        // Đăng ký các processors
        register(Action.LOGIN, (req, session) -> {
            Response<UserDTO> res = userController.login((LoginRequest) req);
            if (res.isSuccess() && res.getData() != null) {
                res.setAuthenticatedUserId(res.getData().getId());
            }
            return res;
        });
        register(Action.REGISTER, (req, session) -> userController.register((RegisterRequest) req));
        register(Action.UPDATE_PROFILE, (req, session) -> userController.updateProfile((UpdateProfileRequest) req));
        register(Action.GET_PROFILE, (req, session) -> userController.getProfile((GetProfileRequest) req));

        register(Action.CREATE_AUCTION, (req, session) -> auctionController.createAuction((CreateAuctionRequest) req));
        register(Action.CANCEL_AUCTION, (req, session) -> auctionController.cancelAuction((CancelAuctionRequest) req, session == null ? null : session.getUserId()));
        register(Action.PLACE_BID, (req, session) -> auctionController.placeBid((PlaceBidRequest) req));
        
        register(Action.SET_AUTO_BID, (req, session) -> auctionController.registerAutoBid(toRegisterAutoBidRequest((SetAutoBidRequest) req)));
        register(Action.REGISTER_AUTO_BID, (req, session) -> auctionController.registerAutoBid((RegisterAutoBidRequest) req));
        register(Action.CANCEL_AUTO_BID, (req, session) -> auctionController.cancelAutoBid((CancelAutoBidRequest) req));
        
        register(Action.GET_AUCTION_DETAIL, (req, session) -> auctionController.getAuctionDetail((GetAuctionDetailRequest) req));
        register(Action.LIST_AUCTIONS, (req, session) -> auctionController.listAuctions((ListAuctionRequest) req));
        register(Action.SET_BID_STEP, (req, session) -> auctionController.setBidStep((SetBidStepRequest) req));
        
        register(Action.UNSUBSCRIBE_AUCTION, (req, session) -> handleUnsubscribe((UnsubscribeAuctionRequest) req, session));
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

        ActionProcessor<Request> processor = processors.get(request.getAction());
        if (processor == null) {
            response = Response.fail("Unsupported action: " + request.getAction());
            response.setRequestId(request.getRequestId());
            return response;
        }

        try {
            response = processor.process(request, session);
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
        if (auctionId == null || auctionId.isBlank()) {
            subscriptionRegistry.unsubscribeAll(session);
            return Response.success("Unsubscribed from all auctions.", true);
        }

        subscriptionRegistry.unsubscribe(auctionId, session);
        return Response.success("Unsubscribed from auction: " + auctionId, true);
    }

    private RegisterAutoBidRequest toRegisterAutoBidRequest(SetAutoBidRequest request) {
        return new RegisterAutoBidRequest(
                request.getAuctionId(), request.getMaxBid(), request.getIncrement(), request.getBidderId());
    }
}
