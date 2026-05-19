package com.auction.server.network;

import com.auction.server.controller.RequestHandler;
import com.auction.share.DTO.Action;
import com.auction.share.DTO.GetAuctionDetailRequest;
import com.auction.share.DTO.Request;
import com.auction.share.DTO.Response;
import com.auction.share.DTO.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class AuctionServer implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionServer.class);

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
    // Client <====== socket ======> Server
    public void run() {
        ClientSession session = null;
        Socket socket = clientSocket;

        try {
            LOGGER.info("Client connected: {}", socket.getRemoteSocketAddress());

            ObjectOutputStream outputStream =
                    new ObjectOutputStream(socket.getOutputStream());

            outputStream.flush();

            ObjectInputStream inputStream =
                    new ObjectInputStream(socket.getInputStream());

            session = new ClientSession(outputStream);

            while (!socket.isClosed()) {

                Object incoming = inputStream.readObject();

                if (!(incoming instanceof Request request)) {
                    LOGGER.warn("Received invalid payload type: {}",
                            incoming == null ? "null" : incoming.getClass().getName());
                    session.send(Response.fail("Invalid request payload."));
                    continue;
                }

                // Inject userId vào request từ session (hỗ trợ withUserId pattern)
                Request authedRequest = (session.getUserId() != null)
                        ? request.withUserId(session.getUserId())
                        : request;

                long startTime = System.currentTimeMillis();
                LOGGER.info("Received request: requestId={}, action={}, type={}",
                        request.getRequestId(),
                        request.getAction(),
                        request.getClass().getSimpleName()
                );

                Response<?> response =
                        requestHandler.handle(authedRequest, session);

                // Sau khi login thành công, lưu userId vào session
                if (Action.LOGIN.equals(request.getAction()) && response.isSuccess() && response.getData() instanceof UserDTO userDTO) {
                    session.setUserId(userDTO.getId());
                    LOGGER.info("Session authenticated for userId={}", userDTO.getId());
                }

                long elapsedMs = System.currentTimeMillis() - startTime;
                LOGGER.info("Processed request: requestId={}, action={}, success={}, durationMs={}",
                        request.getRequestId(),
                        request.getAction(),
                        response.isSuccess(),
                        elapsedMs
                );

                if (response.isSuccess()) {
                    if (request instanceof GetAuctionDetailRequest detailRequest) {
                        subscriptionRegistry.subscribe(
                                detailRequest.getAuctionId(),
                                session
                        );
                    }
                }

                session.send(response);
                LOGGER.info("Sent response: requestId={}, action={}, success={}, message={}",
                        request.getRequestId(),
                        request.getAction(),
                        response.isSuccess(),
                        response.getMessage()
                );
            }

        } catch (EOFException ignored) {
            LOGGER.info("Client disconnected gracefully (EOF).");

        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Connection handler failed: {}", e.getMessage(), e);
        } catch (Throwable t) {
            LOGGER.error("Unexpected fatal error in client handler: {}", t.getMessage(), t);

        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }

            if (session != null) {
                subscriptionRegistry.unsubcribe(session);
            }
            LOGGER.info("Client session closed.");
        }
    }
}
