package com.auction.server.controller;

import com.auction.server.service.UserService;
import com.auction.share.DTO.LoginRequest;
import com.auction.share.DTO.RegisterRequest;
import com.auction.share.DTO.Response;
import com.auction.share.DTO.UpdateProfileRequest;
import com.auction.share.DTO.UserDTO;
import com.auction.share.enums.Role;
import com.auction.share.models.user.Admin;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;

public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public Response<UserDTO> login(LoginRequest request) throws Exception {
        User user = userService.login(request.getUsername(), request.getPassword());
        return Response.success("Login success.", toUserDTO(user));
    }

    public Response<UserDTO> register(RegisterRequest request) throws Exception {
        User user = toUser(request);
        userService.register(user);
        return Response.success("Register success.", toUserDTO(user));
    }

    public Response<UserDTO> updateProfile(UpdateProfileRequest request) throws Exception {
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            return Response.fail("User id is required.");
        }

        if(request.getPassword() != null && !request.getPassword().isBlank()){
            userService.updatePassword(request.getUserId(), request.getPassword());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            userService.updateEmail(request.getUserId(), request.getEmail());
        }
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            userService.updateAddress(request.getUserId(), request.getAddress());
        }

        User user = userService.getById(request.getUserId());
        return Response.success("Profile updated successfully.", toUserDTO(user));
    }

    private User toUser(RegisterRequest request) {
        Role role = Role.valueOf(request.getRole().trim().toUpperCase());
        return switch (role) {
            case BIDDER -> new Bidder(
                    request.getUsername(),
                    request.getPassword(),
                    request.getFullName(),
                    request.getPhoneNumber(),
                    request.getEmail(),
                    request.getAddress()
            );
            case SELLER -> new Seller(
                    request.getUsername(),
                    request.getPassword(),
                    request.getFullName(),
                    request.getPhoneNumber(),
                    request.getEmail(),
                    request.getAddress()
            );
            case ADMIN -> new Admin(
                    request.getUsername(),
                    request.getPassword(),
                    request.getFullName(),
                    1
            );
        };
    }

    private UserDTO toUserDTO(User user) {
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
}
