package com.auction.server.network;

import com.auction.server.controller.RequestHandler;
import com.auction.share.DTO.GetAuctionDetailRequest;
import com.auction.share.DTO.Request;
import com.auction.share.DTO.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class AuctionServer implements Runnable {
    private final Socket clientSocket;
    private final RequestHandler requestHandler;
    private final AuctionSubscriptionRegistry subscriptionRegistry;

    public AuctionServer(
            Socket clientSocket,
            RequestHandler requestHandler,
            AuctionSubscriptionRegistry subscriptionRegistry
    ) {
        this.clientSocket = clientSocket;
        this.requestHandler = requestHandler;
        this.subscriptionRegistry = subscriptionRegistry;
    }

    @Override
    public void run() {
        ClientSession session = null;
        try (
                Socket socket = clientSocket;
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())
        ) {
            session = new ClientSession(outputStream);
            while (!socket.isClosed()) {
                Object incoming = inputStream.readObject();
                if (!(incoming instanceof Request request)) {
                    session.send(Response.fail("Invalid request payload."));
                    continue;
                }

                Response<?> response = requestHandler.handle(request, session);
                if (response.isSuccess()) {
                    if (request instanceof GetAuctionDetailRequest detailRequest) {
                        subscriptionRegistry.subscribe(detailRequest.getAuctionId(), session);
                    }
                }
                session.send(response);
            }
        } catch (EOFException ignored) {
            // Client disconnected gracefully.
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                subscriptionRegistry.removeSession(session);
            }
        }
    }
}
