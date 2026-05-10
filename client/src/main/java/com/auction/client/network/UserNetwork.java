package com.auction.client.network;

import com.auction.share.DTO.RegisterRequest;
import com.auction.share.DTO.LoginRequest;
import com.auction.share.DTO.Response;

import java.util.function.Consumer;

public class UserNetwork {
    private final SocketClient socketClient;

    public UserNetwork(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    public void login(LoginRequest request, Consumer<Response<?>> onResponse) {
        socketClient.login(request, onResponse);
    }

    public void signup(RegisterRequest request, Consumer<Response<?>> onResponse) {
        socketClient.send(request, onResponse);
    }
}
