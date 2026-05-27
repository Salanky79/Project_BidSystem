package com.auction.server.mapper;

import com.auction.share.DTO.RegisterRequest;
import com.auction.share.DTO.UserDTO;
import com.auction.share.enums.Role;
import com.auction.share.models.user.Admin;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;

public class MapUserDTO {
    public UserDTO toUserDTO(User user) {
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
                balance,
                balance); // Available balance will be calculated elsewhere or updated after mapping
    }

    public User toUser(RegisterRequest request) {
        Role role = Role.valueOf(request.getRole().trim().toUpperCase());
        // switch expression khởi tạo đối tượng tương ứng theo enum Role
        return switch (role) {
            case BIDDER ->
                    new Bidder(
                            request.getUsername(),
                            request.getPassword(),
                            request.getFullName(),
                            request.getPhoneNumber(),
                            request.getEmail(),
                            request.getAddress());
            case SELLER ->
                    new Seller(
                            request.getUsername(),
                            request.getPassword(),
                            request.getFullName(),
                            request.getPhoneNumber(),
                            request.getEmail(),
                            request.getAddress());
            case ADMIN ->
                    new Admin(request.getUsername(), request.getPassword(), request.getFullName(), 1);
        };
    }

}
