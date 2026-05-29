package com.auction.server.network;

import com.auction.server.controller.RequestDispatcher;
import com.auction.share.DTO.GetAuctionDetailRequest;
import com.auction.share.DTO.Request;
import com.auction.share.DTO.Response;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Luồng Server (Runnable) chuyên trách xử lý kết nối socket và thông điệp cho một Client cụ thể.
 */
public class ClientConnectionHandler implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnectionHandler.class);

  private final Socket clientSocket;
  private final RequestDispatcher requestDispatcher;
  private final AuctionSubscriptionRegistry subscriptionRegistry;

  public ClientConnectionHandler(
      Socket clientSocket,
      RequestDispatcher requestDispatcher,
      AuctionSubscriptionRegistry subscriptionRegistry) {
    this.clientSocket = clientSocket;
    this.requestDispatcher = requestDispatcher;
    this.subscriptionRegistry = subscriptionRegistry;
  }

  @Override
  // chạy luồng lắng nghe và xử lý dữ liệu liên tục từ Client qua socket
  public void run() {
    ClientSession session = null;
    Socket socket = clientSocket;

    try {
      LOGGER.info("Client connected: {}", socket.getRemoteSocketAddress());

      ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

      outputStream.flush();

      ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

      session = new ClientSession(outputStream);

      while (!socket.isClosed()) {

        Object incoming = inputStream.readObject();

        if (!(incoming instanceof Request request)) {
          LOGGER.warn(
              "Received invalid payload type: {}",
              incoming == null ? "null" : incoming.getClass().getName());
          session.send(Response.fail("Invalid request payload."));
          continue;
        }

        // tự động nhúng userId từ phiên (session) vào request để xác thực
        // giữ nguyên requestId gốc để client callback khớp đúng
        Request authedRequest;
        if (session.getUserId() != null) {
            authedRequest = request.withUserId(session.getUserId());
            authedRequest.setRequestId(request.getRequestId());
        } else {
            authedRequest = request;
        }

        long startTime = System.currentTimeMillis();
        LOGGER.info(
            "Received request: requestId={}, action={}, type={}",
            request.getRequestId(),
            request.getAction(),
            request.getClass().getSimpleName());

        Response<?> response = requestDispatcher.handle(authedRequest, session);

        // lưu userId vào session ngay sau khi đăng nhập thành công
        if (response.getAuthenticatedUserId() != null) {
          session.setUserId(response.getAuthenticatedUserId());
          LOGGER.info("Session authenticated for userId={}", response.getAuthenticatedUserId());
        }

        long elapsedMs = System.currentTimeMillis() - startTime;
        LOGGER.info(
            "Processed request: requestId={}, action={}, success={}, durationMs={}",
            request.getRequestId(),
            request.getAction(),
            response.isSuccess(),
            elapsedMs);

        session.send(response);
        if (response.isSuccess()) {
          if (request instanceof GetAuctionDetailRequest detailRequest) {
            subscriptionRegistry.subscribe(detailRequest.getAuctionId(), session);
          }
        }
        
        LOGGER.info(
            "Sent response: requestId={}, action={}, success={}, message={}",
            request.getRequestId(),
            request.getAction(),
            response.isSuccess(),
            response.getMessage());
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
        subscriptionRegistry.unsubscribeAll(session);
      }
      LOGGER.info("Client session closed.");
    }
  }
}
