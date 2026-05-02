package com.auction.server.controller;

import com.auction.share.DTO.Action;
import com.auction.share.DTO.LoginRequest;
import com.auction.share.DTO.RegisterRequest;
import com.auction.share.DTO.Request;
import com.auction.share.DTO.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RequestRouter {
    private final Map<String, Function<Request, Response<?>>> routes = new HashMap<>();

    public RequestRouter() {
        routes.put(Action.LOGIN, this::handleLogin);
        routes.put(Action.REGISTER, this::handleRegister);
    }

    public Response<?> route(Object payload) {
        if (!(payload instanceof Request request)) {
            return Response.fail("Invalid request payload.");
        }
        if (request.getAction() == null || request.getAction().isBlank()) {
            return Response.fail("Request action is required.");
        }

        Function<Request, Response<?>> handler = routes.get(request.getAction());
        if (handler == null) {
            return Response.fail("Unsupported action: " + request.getAction());
        }

        return handler.apply(request);
    }

    private Response<?> handleLogin(Request request) {
        if (!(request instanceof LoginRequest loginRequest)) {
            return Response.fail("Invalid LOGIN request.");
        }
        return UserController.login(loginRequest);
    }

    private Response<?> handleRegister(Request request) {
        if (!(request instanceof RegisterRequest registerRequest)) {
            return Response.fail("Invalid REGISTER request.");
        }
        return UserController.register(registerRequest);
    }
}
