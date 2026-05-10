package com.auction.server.network;

import com.auction.server.controller.RequestHandler;
import com.auction.share.DTO.Request;
import com.auction.share.DTO.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final RequestHandler requestHandler;

    public ClientHandler(Socket clientSocket, RequestHandler requestHandler) {
        this.clientSocket = clientSocket;
        this.requestHandler = requestHandler;
    }

    @Override
    public void run() {
        try (
                Socket socket = clientSocket;
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())
        ) {
            while (!socket.isClosed()) {
                Object incoming = inputStream.readObject();
                if (!(incoming instanceof Request request)) {
                    outputStream.writeObject(Response.fail("Invalid request payload."));
                    outputStream.flush();
                    continue;
                }

                Response<?> response = requestHandler.handle(request);
                outputStream.writeObject(response);
                outputStream.flush();
            }
        } catch (EOFException ignored) {
            // Client disconnected gracefully.
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
