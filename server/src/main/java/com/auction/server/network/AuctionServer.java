package com.auction.server.network;

import com.auction.server.controller.RequestHandler;
import com.auction.share.DTO.Action;
import com.auction.share.DTO.GetAuctionDetailRequest;
import com.auction.share.DTO.Request;
import com.auction.share.DTO.Response;
import com.auction.share.DTO.UserDTO;
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

// xử lý từng request một chứ ko phải song song
public class AuctionServer implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuctionServer.class); // in log
  // có level log
  // có gile log           >>        println (in console )
  // có timestamp
  // dễ debug
  private final Socket clientSocket;
  private final RequestHandler requestHandler;
  private final AuctionSubscriptionRegistry subscriptionRegistry;

  public AuctionServer(
      Socket clientSocket,
      RequestHandler requestHandler,
      AuctionSubscriptionRegistry subscriptionRegistry) {
    this.clientSocket = clientSocket;
    this.requestHandler = requestHandler;
    this.subscriptionRegistry = subscriptionRegistry;
  }

  @Override
  // chạy luồng lắng nghe và xử lý dữ liệu liên tục từ Client qua socket
  public void run() {
    ClientSession session = null; // thông tin của client
    Socket socket = clientSocket; // kết nối giữa client và server

    try {
      LOGGER.info("Client connected: {}", socket.getRemoteSocketAddress());

      ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
      // luồng để sever gửi về client

      outputStream.flush();
      // đẩy dữ liệu ngay lập tức ra network

      ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
      // luồng để sever nhận về từ client

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
        // ko cần gửi ID
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

        Response<?> response = requestHandler.handle(authedRequest, session);

        // lưu userId vào session ngay sau khi đăng nhập thành công
        if (Action.LOGIN.equals(request.getAction())
            && response.isSuccess()
            && response.getData() instanceof UserDTO userDTO) {

          session.setUserId(userDTO.getId());
          // gắn ID vào session

          LOGGER.info("Session authenticated for userId={}", userDTO.getId());
        }

        // Client gửi request → server xử lý → đo xem mất bao nhiêu ms
        long elapsedMs = System.currentTimeMillis() - startTime;
        LOGGER.info(
            "Processed request: requestId={}, action={}, success={}, durationMs={}",
            request.getRequestId(),
            request.getAction(),
            response.isSuccess(),
            elapsedMs);

        // “Client nào đang mở màn hình chi tiết auction này”
        if (response.isSuccess()) {
          if (request instanceof GetAuctionDetailRequest detailRequest) {
            subscriptionRegistry.subscribe(detailRequest.getAuctionId(), session);
          }
        }

        session.send(response);
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
