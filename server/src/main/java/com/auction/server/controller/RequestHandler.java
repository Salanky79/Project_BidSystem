package com.auction.server.controller;

import com.auction.server.network.AuctionSubscriptionRegistry;
import com.auction.server.service.AutoBidService;
import com.auction.server.network.ClientSession;
import com.auction.server.service.UserService;
import com.auction.share.DTO.Action;
import com.auction.share.DTO.LoginRequest;
import com.auction.share.DTO.RegisterRequest;
import com.auction.share.DTO.Request;
import com.auction.share.DTO.Response;
import com.auction.share.DTO.UpdateProfileRequest;
import com.auction.share.DTO.GetProfileRequest;
import com.auction.share.DTO.CancelAutoBidRequest;
import com.auction.share.DTO.CreateAuctionRequest;
import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.DTO.RegisterAutoBidRequest;
import com.auction.share.DTO.SetAutoBidRequest;
import com.auction.share.DTO.SetBidStepRequest;
import com.auction.share.DTO.ExtendEndTimeRequest;
import com.auction.share.DTO.GetAuctionDetailRequest;
import com.auction.share.DTO.ListAuctionRequest;
import com.auction.share.DTO.UnsubscribeAuctionRequest;
import com.auction.server.service.AuctionService;
import com.auction.share.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);
    private final UserController userController;
    private final AuctionController auctionController;
    private final AuctionSubscriptionRegistry subscriptionRegistry;

    // quet dinh xem request se di dau
    public RequestHandler(
            UserService userService,
            AuctionService auctionService,
            AutoBidService autoBidService,
            AuctionSubscriptionRegistry subscriptionRegistry
    ) {
        // can service riêng cho từng client
        this.userController = new UserController(userService);
        this.auctionController = new AuctionController(auctionService, autoBidService);
        // real time dealer
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
            response = switch (request.getAction()) {
                case Action.LOGIN -> userController.login((LoginRequest) request);
                case Action.REGISTER -> userController.register((RegisterRequest) request);
                case Action.UPDATE_PROFILE -> userController.updateProfile((UpdateProfileRequest) request);
                case Action.GET_PROFILE -> userController.getProfile((GetProfileRequest) request);
                case Action.CREATE_AUCTION -> auctionController.createAuction((CreateAuctionRequest) request);
                case Action.CANCEL_AUCTION -> auctionController.cancelAuction((com.auction.share.DTO.CancelAuctionRequest) request);
                case Action.PLACE_BID -> auctionController.placeBid((PlaceBidRequest) request);
                case Action.SET_AUTO_BID -> auctionController.registerAutoBid(toRegisterAutoBidRequest((SetAutoBidRequest) request));
                case Action.REGISTER_AUTO_BID -> auctionController.registerAutoBid((RegisterAutoBidRequest) request);
                case Action.CANCEL_AUTO_BID -> auctionController.cancelAutoBid((CancelAutoBidRequest) request);
                case Action.GET_AUCTION_DETAIL -> auctionController.getAuctionDetail((GetAuctionDetailRequest) request);
                case Action.LIST_AUCTIONS -> auctionController.listAuctions((ListAuctionRequest) request);
                case Action.SET_BID_STEP -> auctionController.setBidStep((SetBidStepRequest) request);
                case Action.EXTEND_END_TIME -> auctionController.extendEndTime((ExtendEndTimeRequest) request);
                case Action.UNSUBSCRIBE_AUCTION -> handleUnsubscribe((UnsubscribeAuctionRequest) request, session);
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
                request.getAuctionId(),
                request.getMaxBid(),
                request.getIncrement(),
                request.getBidderId()
        );
    }
}
