package com.auction.server.network;

import com.auction.share.DTO.Response;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Thông tin phiên kết nối của client.
 */
public class ClientSession {
    private final ObjectOutputStream outputStream;
    private volatile String userId;

    public ClientSession(ObjectOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Gửi response về client theo cơ chế đồng bộ.
    public void send(Response<?> response) throws IOException {
        // Đồng bộ hóa outputStream để tránh race condition.
        synchronized (outputStream){
            outputStream.writeObject(response);
            outputStream.flush();
        }
    }
}