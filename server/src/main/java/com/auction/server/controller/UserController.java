package com.auction.server.controller;

import com.auction.server.service.UserService;
import com.auction.share.DTO.LoginRequest;
import com.auction.share.DTO.RegisterRequest;
import com.auction.share.DTO.Response;
import com.auction.share.DTO.UserDTO;
import com.auction.share.enums.Role;
import com.auction.share.models.user.Admin;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;

public class UserController {
    public static Response<UserDTO> login(LoginRequest request) {
        try {
            User user = UserService.login(request.getUsername(), request.getPassword());
            return Response.success("Login success.", toUserDTO(user));
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    public static Response<UserDTO> register(RegisterRequest request) {
        try {
            User user = createUserFromRequest(request);
            UserService.register(user);
            return Response.success("Register success.", toUserDTO(user));
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    private static User createUserFromRequest(RegisterRequest request) {
        Role role = parseRole(request.getRole());
        switch (role) {
            case BIDDER:
                return new Bidder(
                        request.getUsername(),
                        request.getPassword(),
                        request.getFullName(),
                        request.getPhoneNumber(),
                        request.getEmail(),
                        request.getAddress()
                );
            case SELLER:
                return new Seller(
                        request.getUsername(),
                        request.getPassword(),
                        request.getFullName(),
                        request.getPhoneNumber(),
                        request.getEmail()
                );
            case ADMIN:
                return new Admin(
                        request.getUsername(),
                        request.getPassword(),
                        request.getFullName(),
                        1
                );
            default:
                throw new IllegalArgumentException("Unsupported role: " + request.getRole());
        }
    }

    private static Role parseRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role is required.");
        }
        try {
            return Role.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

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
}
