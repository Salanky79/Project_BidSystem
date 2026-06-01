package com.auction.server.controller;

import com.auction.share.DTO.Response;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.exceptions.AuthenticationException;
import com.auction.share.exceptions.DuplicateResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.function.Function;

public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final Map<Class<? extends Exception>, Function<Exception, Response<?>>> handlers = Map.of(
        ValidationException.class, e -> Response.fail(e.getMessage()),
        ClassCastException.class, e -> Response.fail("Request type does not match action."),
        AuthenticationException.class, e -> Response.fail(e.getMessage()),
        DuplicateResourceException.class, e -> Response.fail(e.getMessage())
    );

    public Response<?> handle(Exception e, String action) {
        return handlers.entrySet().stream()
                .filter(entry -> entry.getKey().isInstance(e)) // 1. Tìm xem lỗi ném ra thuộc nhóm lỗi nào đã đăng ký
                .findFirst()                                   // 2. Lấy bộ xử lý đầu tiên khớp
                .map(entry -> entry.getValue().apply(e))       // 3. Thực thi hàm xử lý lỗi
                .orElseGet(() -> {                             // 4. Nếu lỗi lạ (chưa đăng ký), chạy khối mặc định này
                    LOGGER.error("Unhandled request processing error for action={}", action, e);
                    return Response.fail("An internal error occurred.");
                });
    }
}
