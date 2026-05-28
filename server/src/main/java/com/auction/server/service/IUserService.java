package com.auction.server.service;

import com.auction.share.DTO.ProfileDTO;
import com.auction.share.DTO.UpdateProfileRequest;
import com.auction.share.exceptions.AuthenticationException;
import com.auction.share.exceptions.DuplicateResourceException;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.user.User;

import java.sql.SQLException;

public interface IUserService {
    boolean register(User user) throws SQLException, ValidationException, DuplicateResourceException;
    User login(String username, String password) throws SQLException, AuthenticationException, ValidationException;
    User updateProfile(UpdateProfileRequest request) throws SQLException, ValidationException;
    ProfileDTO getProfile(String id) throws SQLException, ValidationException;
}
