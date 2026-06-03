package com.auction.server.controller;

import com.auction.server.service.AuctionSubscriptionRegistry;
import com.auction.server.network.ClientSession;
import com.auction.share.DTO.Action;
import com.auction.share.DTO.LoginRequest;
import com.auction.share.DTO.Request;
import com.auction.share.DTO.Response;
import com.auction.share.DTO.UnsubscribeAuctionRequest;
import com.auction.share.DTO.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    private UserController userController;

    @Mock
    private AuctionController auctionController;

    @Mock
    private AuctionSubscriptionRegistry subscriptionRegistry;

    @Test
    void handle_nullAction_returnsFailResponse() {
        RequestDispatcher handler = new RequestDispatcher(userController, auctionController, subscriptionRegistry);

        Response<?> response = handler.handle(new RawRequest(null), null);

        assertFalse(response.isSuccess());
        assertEquals("Action is required.", response.getMessage());
    }

    @Test
    void handle_requestIdPropagated() throws Exception {
        RequestDispatcher handler = new RequestDispatcher(userController, auctionController, subscriptionRegistry);
        
        Response<UserDTO> res = Response.success("OK", null);
        when(userController.login(Mockito.any(LoginRequest.class))).thenReturn(res);

        LoginRequest request = new LoginRequest("alice", "pwd");
        setRequestId(request, "req-123");

        Response<?> response = handler.handle(request, null);

        assertEquals("req-123", response.getRequestId());
    }

    @Test
    void handle_classCastException_returnsFailResponse() {
        RequestDispatcher handler = new RequestDispatcher(userController, auctionController, subscriptionRegistry);

        Response<?> response = handler.handle(new RawRequest(Action.LOGIN), null);

        assertFalse(response.isSuccess());
        assertEquals("Request type does not match action.", response.getMessage());
    }

    @Test
    void handle_login_routesToUserController() throws Exception {
        RequestDispatcher handler = new RequestDispatcher(userController, auctionController, subscriptionRegistry);
        
        Response<UserDTO> res = Response.success("OK", null);
        when(userController.login(Mockito.any(LoginRequest.class))).thenReturn(res);

        LoginRequest req = new LoginRequest("alice", "pwd");
        Response<?> response = handler.handle(req, null);

        assertTrue(response.isSuccess());
        verify(userController).login(req);
    }

    @Test
    void handle_unsubscribe_nullAuctionId_unsubscribeAll() throws Exception {
        RequestDispatcher handler = new RequestDispatcher(userController, auctionController, subscriptionRegistry);
        ClientSession session = new ClientSession(new ObjectOutputStream(new ByteArrayOutputStream()));

        Response<?> response = handler.handle(new UnsubscribeAuctionRequest(null), session);

        assertTrue(response.isSuccess());
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
