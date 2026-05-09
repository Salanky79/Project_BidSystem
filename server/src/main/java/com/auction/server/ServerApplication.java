package com.auction.server;


import com.auction.server.controller.UserController;
import com.auction.server.dao.UserDAO;
import com.auction.server.service.UserService;
import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import org.jetbrains.annotations.NotNull;


import java.lang.reflect.Type;

public class ServerApplication {

    private static final Gson gson = new Gson();

    public static void main(String[] args) {

        Javalin app = Javalin.create(config -> {
            // Cấu hình Gson làm JSON mapper cho Javalin
            config.jsonMapper(new JsonMapper() {
                @Override
                public @NotNull String toJsonString(@NotNull Object obj, @NotNull Type type) {
                    return gson.toJson(obj, type);
                }

                @Override
                public <T> @NotNull T fromJsonString(@NotNull String json, @NotNull Type targetType) {
                    return gson.fromJson(json, targetType);
                }
            });
        });

        UserDAO userDao = new UserDAO();
        UserService userService = new UserService(userDao);
        UserController userController = new UserController(userService);

        // ──────────── REST API Routes ────────────
        app.post("/api/auth/login", userController::login);
        app.post("/api/auth/register", userController::register);
        app.put("/api/users/password", userController::updatePassword);
        app.put("/api/users/email", userController::updateEmail);
        app.put("/api/users/address", userController::updateAddress);

        // TODO: Thêm routes cho Auction khi có AuctionController
        // app.get("/api/auctions", AuctionController::list);
        // app.get("/api/auctions/{id}", AuctionController::getDetail);
        // app.post("/api/auctions", AuctionController::create);

        // ──────────── WebSocket cho Bidding Realtime ────────────


        // Start server tại cổng 8080
        app.start(8080);
        System.out.println("Server dau gia dang chay tai cong 8080...");
        System.out.println("  REST API:  http://localhost:8080/api/...");
        System.out.println("  WebSocket: ws://localhost:8080/ws/auction");
    }
}
