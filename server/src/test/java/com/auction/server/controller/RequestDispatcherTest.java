package com.auction.server.controller;

import com.auction.server.network.AuctionSubscriptionRegistry;
import com.auction.server.network.ClientSession;
import com.auction.server.service.IAuctionService;
import com.auction.server.service.AutoBidService;
import com.auction.server.service.IUserService;
import com.auction.server.service.BidCoordinator;
import com.auction.server.service.AuctionQueryService;
import com.auction.share.DTO.Action;
import com.auction.share.DTO.LoginRequest;
import com.auction.share.DTO.Request;
import com.auction.share.DTO.Response;
import com.auction.share.DTO.UnsubscribeAuctionRequest;
import com.auction.share.models.user.Bidder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestDispatcherTest {

    @Mock
    private IUserService userService;

    @Mock
    private IAuctionService auctionService;

    @Mock
    private AutoBidService autoBidService;

    @Mock
    private BidCoordinator bidCoordinator;

    @Mock
    private AuctionQueryService auctionQueryService;

    @Mock
    private AuctionSubscriptionRegistry subscriptionRegistry;

    @Mock
    private com.auction.server.service.BroadcastService broadcastService;

    @Test
    void handle_nullAction_returnsFailResponse() {
        RequestDispatcher handler = new RequestDispatcher(userService, auctionService, autoBidService, bidCoordinator, auctionQueryService, subscriptionRegistry, broadcastService);

        Response<?> response = handler.handle(new RawRequest(null), null);

        assertFalse(response.isSuccess());
        assertEquals("Action is required.", response.getMessage());
    }

    @Test
    void handle_requestIdPropagated() throws Exception {
        RequestDispatcher handler = new RequestDispatcher(userService, auctionService, autoBidService, bidCoordinator, auctionQueryService, subscriptionRegistry, broadcastService);
        Bidder bidder = new Bidder("alice", "pwd", "Alice", "090", "alice@mail.com", "HCM");
        bidder.setID("u-1");
        when(userService.login("alice", "pwd")).thenReturn(bidder);

        LoginRequest request = new LoginRequest("alice", "pwd");
        setRequestId(request, "req-123");

        Response<?> response = handler.handle(request, null);

        assertEquals("req-123", response.getRequestId());
    }

    @Test
    void handle_classCastException_returnsFailResponse() {
        RequestDispatcher handler = new RequestDispatcher(userService, auctionService, autoBidService, bidCoordinator, auctionQueryService, subscriptionRegistry, broadcastService);

        Response<?> response = handler.handle(new RawRequest(Action.LOGIN), null);

        assertFalse(response.isSuccess());
        assertEquals("Request type does not match action.", response.getMessage());
    }

    @Test
    void handle_login_routesToUserController() throws Exception {
        RequestDispatcher handler = new RequestDispatcher(userService, auctionService, autoBidService, bidCoordinator, auctionQueryService, subscriptionRegistry, broadcastService);
        Bidder bidder = new Bidder("alice", "pwd", "Alice", "090", "alice@mail.com", "HCM");
        bidder.setID("u-1");
        when(userService.login("alice", "pwd")).thenReturn(bidder);

        Response<?> response = handler.handle(new LoginRequest("alice", "pwd"), null);

        assertTrue(response.isSuccess());
        verify(userService).login("alice", "pwd");
    }

    @Test
    void handle_unsubscribe_nullAuctionId_unsubscribeAll() throws Exception {
        RequestDispatcher handler = new RequestDispatcher(userService, auctionService, autoBidService, bidCoordinator, auctionQueryService, subscriptionRegistry, broadcastService);
        ClientSession session = new ClientSession(new ObjectOutputStream(new ByteArrayOutputStream()));

        Response<?> response = handler.handle(new UnsubscribeAuctionRequest(null), session);

        assertTrue(response.isSuccess());
        verify(subscriptionRegistry).unsubscribeAll(session);
    }

    private static void setRequestId(Request request, String value) throws Exception {
        Field field = Request.class.getDeclaredField("requestId");
        field.setAccessible(true);
        field.set(request, value);
    }

    private static class RawRequest extends Request {
        private RawRequest(String action) {
            super(action);
        }
    }
}
