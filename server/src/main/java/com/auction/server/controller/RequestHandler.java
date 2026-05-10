package com.auction.server.controller;

import com.auction.server.service.UserService;
import com.auction.share.DTO.Action;
import com.auction.share.DTO.LoginRequest;
import com.auction.share.DTO.RegisterRequest;
import com.auction.share.DTO.Request;
import com.auction.share.DTO.Response;
import com.auction.share.DTO.UpdateProfileRequest;

public class RequestHandler {
    private final UserController userController;

    public RequestHandler(UserService userService) {
        this.userController = new UserController(userService);
    }

    public Response<?> handle(Request request) {
        Response<?> response;
        if (request.getAction() == null) {
            response = Response.fail("Action is required.");
            response.setResquestId(request.getRequestId());
            return response;
        }

        try {
            response = switch (request.getAction()) {
                case Action.LOGIN -> userController.login((LoginRequest) request);
                case Action.REGISTER -> userController.register((RegisterRequest) request);
                case Action.UPDATE_PROFILE -> userController.updateProfile((UpdateProfileRequest) request);
                default -> Response.fail("Unsupported action: " + request.getAction());
            };
        } catch (ClassCastException e) {
            response = Response.fail("Request type does not match action.");
        } catch (Exception e) {
            response = Response.fail(e.getMessage());
        }
        response.setResquestId(request.getRequestId());
        return response;
    }
}
