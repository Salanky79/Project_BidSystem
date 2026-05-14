package com.auction.server.network;

import com.auction.share.DTO.Response;

import java.io.IOException;
import java.io.ObjectOutputStream;


public class ClientSession {
    private final ObjectOutputStream outputStream;

    public ClientSession(ObjectOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void send(Response<?> response) throws IOException {
        synchronized (outputStream) {
            outputStream.writeObject(response);
            outputStream.flush();
        }
    }
}