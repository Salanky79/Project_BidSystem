package com.auction.client.service;

import com.auction.client.network.SocketClient;
import com.auction.client.session.SessionManager;
import com.auction.share.DTO.LoginRequest;
import com.auction.share.DTO.GetProfileRequest;
import com.auction.share.DTO.RegisterRequest;
import com.auction.share.DTO.Response;
import com.auction.share.DTO.UserDTO;
import com.auction.share.exceptions.ValidationException;

import java.util.function.Consumer;

public class UserService {
    private final SocketClient socketClient;
    private final SessionManager sessionManager;

    public UserService(SocketClient socketClient, SessionManager sessionManager) {
        this.socketClient = socketClient;
        this.sessionManager = sessionManager;
    }

    public void login(LoginRequest request, Consumer<Response<?>> onResponse) throws ValidationException {
        if (request == null) {
            throw new ValidationException("Request is required.");
        }
        String username = request.getUsername();
        String password = request.getPassword();
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new ValidationException("Username va Password khong duoc de trong!");
        }
        socketClient.send(request, response -> {
            if (response != null && response.isSuccess() && response.getData() instanceof UserDTO userDTO) {
                sessionManager.setCurrentUserId(userDTO.getId());
            }
            if (onResponse != null) {
                onResponse.accept(response);
            }
        });
    }


    public void signup(RegisterRequest request, Consumer<Response<?>> onResponse) throws ValidationException {
        if (request == null) {
            throw new ValidationException("Request is required.");
        }

        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new ValidationException("Ho ten khong duoc de trong!");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new ValidationException("Username khong duoc de trong!");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ValidationException("Password khong duoc de trong!");
        }
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            throw new ValidationException("Vai tro khong hop le!");
        }

        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new ValidationException("So dien thoai khong duoc de trong!");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email khong duoc de trong!");
        }
        if (request.getAddress() == null || request.getAddress().isBlank()) {
            throw new ValidationException("Dia chi khong hop le!");
        }

        String normalizedPhone = request.getPhoneNumber().trim();
        if (!normalizedPhone.matches("^0\\d{8,10}$")) {
            throw new ValidationException("So dien thoai khong hop le!");
        }

        String normalizedEmail = request.getEmail().trim();
        if (!normalizedEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("Email khong hop le!");
        }

        socketClient.send(request, response -> {
            if (response != null && response.isSuccess() && response.getData() instanceof UserDTO userDTO) {
                sessionManager.setCurrentUserId(userDTO.getId());
            }
            if (onResponse != null) {
                onResponse.accept(response);
            }
        });
    }

   
    public void getProfile(Consumer<Response<?>> onResponse) {
        socketClient.send(new GetProfileRequest(null), onResponse);
    }

}
