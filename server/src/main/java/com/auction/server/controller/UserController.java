package com.auction.server.controller;

import com.auction.server.dao.UserDAO;
import com.auction.server.service.UserService;
import com.auction.share.DTO.Response;
import com.auction.share.DTO.UserDTO;
import com.auction.share.enums.Role;
import com.auction.share.exceptions.AuctionSystemException;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.user.Admin;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.http.Context;

import java.sql.SQLException;

public class UserController {
    private static final UserService USER_SERVICE = new UserService(new UserDAO());

    // ──────────── POST /api/auth/login ────────────
    public static void login(Context ctx) {
        try {
            JsonObject body = JsonParser.parseString(ctx.body()).getAsJsonObject();
            String username = getStringOrThrow(body, "username");
            String password = getStringOrThrow(body, "password");

            User user = USER_SERVICE.login(username, password);
            ctx.json(Response.success("Login success.", toUserDTO(user)));
        } catch (AuctionSystemException e) {
            // Lỗi business logic (validation, auth) → trả message cho client
            ctx.status(400).json(Response.fail(e.getMessage()));
        } catch (SQLException e) {
            // Lỗi database → ẩn chi tiết, chỉ trả thông báo chung
            e.printStackTrace();
            ctx.status(500).json(Response.fail("System error. Please try again later."));
        } catch (Exception e) {
            // Lỗi không mong muốn (VD: BCrypt throw IllegalArgumentException nếu hash bị lỗi)
            e.printStackTrace();
            ctx.status(500).json(Response.fail("System error. Please try again later."));
        }
    }

    // ──────────── POST /api/auth/register ────────────
    public static void register(Context ctx) {
        try {
            JsonObject body = JsonParser.parseString(ctx.body()).getAsJsonObject();
            User user = createUserFromJson(body);
            USER_SERVICE.register(user);
            ctx.json(Response.success("Register success.", toUserDTO(user)));
        } catch (AuctionSystemException e) {
            ctx.status(400).json(Response.fail(e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500).json(Response.fail("System error. Please try again later."));
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Response.fail("System error. Please try again later."));
        }
    }

    public static void updatePassword(Context ctx) {
        try {
            JsonObject body = JsonParser.parseString(ctx.body()).getAsJsonObject();
            String userId = getStringOrThrow(body, "userId");
            String password = getStringOrThrow(body, "password");

            boolean updated = USER_SERVICE.updatePassword(userId, password);
            if (!updated) {
                ctx.status(404).json(Response.fail("User not found."));
                return;
            }
            ctx.json(Response.success("Password updated successfully.", null));
        } catch (AuctionSystemException e) {
            ctx.status(400).json(Response.fail(e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500).json(Response.fail("System error. Please try again later."));
        }
    }

    public static void updateEmail(Context ctx) {
        try {
            JsonObject body = JsonParser.parseString(ctx.body()).getAsJsonObject();
            String userId = getStringOrThrow(body, "userId");
            String email = getStringOrThrow(body, "email");

            boolean updated = USER_SERVICE.updateEmail(userId, email);
            if (!updated) {
                ctx.status(404).json(Response.fail("User not found."));
                return;
            }
            ctx.json(Response.success("Email updated successfully.", null));
        } catch (AuctionSystemException e) {
            ctx.status(400).json(Response.fail(e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500).json(Response.fail("System error. Please try again later."));
        }
    }

    public static void updateAddress(Context ctx) {
        try {
            JsonObject body = JsonParser.parseString(ctx.body()).getAsJsonObject();
            String userId = getStringOrThrow(body, "userId");
            String address = getStringOrThrow(body, "address");

            boolean updated = USER_SERVICE.updateAddress(userId, address);
            if (!updated) {
                ctx.status(404).json(Response.fail("User not found."));
                return;
            }
            ctx.json(Response.success("Address updated successfully.", null));
        } catch (AuctionSystemException e) {
            ctx.status(400).json(Response.fail(e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500).json(Response.fail("System error. Please try again later."));
        }
    }

    // ──────────── Helper: Tạo User từ JSON body ────────────

    private static User createUserFromJson(JsonObject body) throws ValidationException {
        Role role = parseRole(getStringOrNull(body, "role"));
        String username = getStringOrNull(body, "username");
        String password = getStringOrNull(body, "password");
        String fullName = getStringOrNull(body, "fullName");
        String phoneNumber = getStringOrNull(body, "phoneNumber");
        String email = getStringOrNull(body, "email");
        String address = getStringOrNull(body, "address");

        switch (role) {
            case BIDDER:
                return new Bidder(username, password, fullName, phoneNumber, email, address);
            case SELLER:
                return new Seller(username, password, fullName, phoneNumber, email, address);
            case ADMIN:
                return new Admin(username, password, fullName, 1);
            default:
                throw new ValidationException("Unsupported role: " + role);
        }
    }

    private static Role parseRole(String role) throws ValidationException {
        if (role == null || role.isBlank()) {
            throw new ValidationException("Role is required.");
        }
        try {
            return Role.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid role: " + role);
        }
    }

    // ──────────── Helper: User → UserDTO ────────────

    private static UserDTO toUserDTO(User user) {
        String phoneNumber = null;
        String email = null;
        String address = null;
        double balance = 0.0;

        if (user instanceof Bidder bidder) {
            phoneNumber = bidder.getPhoneNumber();
            email = bidder.getEmail();
            address = bidder.getAddress();
            balance = bidder.getBalance();
        } else if (user instanceof Seller seller) {
            phoneNumber = seller.getPhoneNumber();
            email = seller.getEmail();
            address = seller.getAddress();
            balance = seller.getBalance();
        }

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().name(),
                phoneNumber,
                email,
                address,
                balance
        );
    }

    // ──────────── JSON Utility ────────────

    private static String getStringOrThrow(JsonObject obj, String key) throws ValidationException {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            throw new ValidationException(key + " is required.");
        }
        return obj.get(key).getAsString();
    }

    private static String getStringOrNull(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        return obj.get(key).getAsString();
    }
}
