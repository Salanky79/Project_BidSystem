package com.auction.server.network;

import com.auction.share.DTO.Response;

import java.io.IOException;
import java.io.ObjectOutputStream;


public class ClientSession {
    private final ObjectOutputStream outputStream;

    public ClientSession(ObjectOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    // wrapper gửi dữ liệu an toàn
    // gui respond tu server ve client
    public void send(Response<?> response) throws IOException {
        // ki thuat khoa block => tranh race condition
        synchronized (outputStream){
            outputStream.writeObject(response);
            outputStream.flush();
        }
    }
}