package com.auction.client.service;

import com.auction.client.network.SocketClient;
import com.auction.client.session.SessionManager;
import com.auction.share.DTO.LoginRequest;
import com.auction.share.DTO.GetProfileRequest;
import com.auction.share.DTO.RegisterRequest;
import com.auction.share.DTO.Response;
import com.auction.share.DTO.UpdateProfileRequest;
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

    // Consumer cho phép nhận vào biến chứa hàm (lambda ->///)
    // onResponse =
    // response -> {
    //    System.out.println(response);
    //}
    // onResponse là callback chạy từ login controller
    // async + callback => tránh tình trạng UI bị đơ
    public void login(LoginRequest request, Consumer<Response<?>> onResponse) throws ValidationException {
        if (request == null) {
            throw new ValidationException("Request is required.");
        }
        String username = request.getUsername();
        String password = request.getPassword();
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new ValidationException("Username va Password khong duoc de trong!");
        }

        // ngay sau khi server trả kết quả thì hàm này sẽ chạy ( đợi kết quả )
        socketClient.send(request, response -> {
            if (response != null && response.isSuccess() && response.getData() instanceof UserDTO userDTO) {
                sessionManager.setCurrentUserId(userDTO.getId());
            }
            if (onResponse != null) {
                // gọi cái hàm ( biến chứa hàm ) // chính là Platform.runlater
                // thực thi callback đang nằm trong Consumer
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

    public void updateProfile(String fullName, String phoneNumber, String email, Consumer<Response<?>> onResponse) throws ValidationException {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Ho ten khong duoc de trong!");
        }
        String normalizedPhone = (phoneNumber == null || phoneNumber.trim().isEmpty()) ? null : phoneNumber.trim();
        if (normalizedPhone != null && !normalizedPhone.matches("^0\\d{8,10}$")) {
            throw new ValidationException("So dien thoai khong hop le!");
        }

        String normalizedEmail = (email == null || email.trim().isEmpty()) ? null : email.trim();
        if (normalizedEmail != null && !normalizedEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("Email khong hop le!");
        }

        socketClient.send(new UpdateProfileRequest(null, fullName.trim(), null, normalizedPhone, normalizedEmail, null), onResponse);
    }

    /** Trả về SessionManager để các controller có thể đọc currentUserId. */
    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
