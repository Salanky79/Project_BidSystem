package com.auction.server.controller;

import com.auction.share.DTO.Response;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RequestHandler implements Runnable {
    private final Socket clientSocket;
    private final RequestRouter router;

    public RequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.router = new RequestRouter();
    }

    @Override
    public void run() {
        try (
                Socket socket = clientSocket;
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream())
        ) {
            while (!socket.isClosed()) {
                Object payload = input.readObject();
                Response<?> response = router.route(payload);
                output.writeObject(response);
                output.flush();
                output.reset();
            }
        } catch (EOFException e) {
            System.out.println("Client disconnected.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
