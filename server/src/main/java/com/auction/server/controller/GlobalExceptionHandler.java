package com.auction.server.controller;

import com.auction.share.DTO.Response;
import com.auction.share.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public Response<?> handle(Exception e, String action) {
        if (e instanceof ClassCastException) {
            return Response.fail("Request type does not match action.");
        } else if (e instanceof ValidationException) {
            return Response.fail(e.getMessage());
        } else {
            LOGGER.error("Unhandled request processing error for action={}", action, e);
            return Response.fail("An internal error occurred.");
        }
    }
}
