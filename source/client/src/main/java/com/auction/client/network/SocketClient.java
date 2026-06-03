package com.auction.client.network;

import com.auction.client.utils.NotificationManager;
import com.auction.share.DTO.Request;
import com.auction.share.DTO.Response;
import javafx.application.Platform;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class SocketClient {
  private final String host;
  private final int port;

  private final ExecutorService receiveExecutor;
  private final Map<String, Consumer<Response<?>>> callbacks;
  private final List<Consumer<Response<?>>> pushListeners;

  private Socket socket;
  private ObjectOutputStream outputStream;
  private ObjectInputStream inputStream;
  private volatile boolean listening;
  private volatile boolean isShuttingDown = false;
  private final Object socketLock = new Object();
  private Runnable onConnectionLost;

  // Executor riêng cho I/O gửi — tránh block caller thread (JavaFX thread)
  private final ExecutorService sendExecutor;

  public SocketClient(String host, int port) {
    this.host = host;
    this.port = port;

    this.receiveExecutor = Executors.newSingleThreadExecutor(
            runnable -> {
              Thread thread = new Thread(runnable);
              thread.setDaemon(true);
              return thread;
            });

    this.callbacks = new ConcurrentHashMap<>();
    this.pushListeners = new CopyOnWriteArrayList<>();


    this.sendExecutor = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "socket-send-thread");
      thread.setDaemon(true);
      return thread;
    });
  }

  public void setOnConnectionLost(Runnable onConnectionLost) {
    this.onConnectionLost = onConnectionLost;
  }



  public void addPushListener(Consumer<Response<?>> listener) {
    if (listener != null) {
      pushListeners.add(listener);
    }
  }

  public void removePushListener(Consumer<Response<?>> listener) {
    if (listener != null) {
      pushListeners.remove(listener);
    }
  }

  // gửi request lên server
  // khi có response => chạy callback onResponse
  public void send(Request request, Consumer<Response<?>> onResponse) {
    // C1: Submit I/O vào sendExecutor — không block caller thread
    sendExecutor.submit(() -> {
      try {
        ensureConnected();
      } catch (IOException e) {
        if (onResponse != null) {
          Platform.runLater(() -> onResponse.accept(Response.fail("Cannot connect to server: " + e.getMessage())));
        }
        return;
      }

      synchronized (socketLock) {
        if (onResponse != null) {
          callbacks.put(request.getRequestId(), onResponse);
        }
        try {
          outputStream.writeObject(request);
          outputStream.flush();
        } catch (IOException e) {
          if (onResponse != null) {
            callbacks.remove(request.getRequestId());
            Platform.runLater(() -> onResponse.accept(Response.fail("Failed to send request: " + e.getMessage())));
          }
          closeConnection();
        }
      }
    });
  }

  private void ensureConnected() throws IOException {
    synchronized (socketLock) {
      if (socket != null && socket.isConnected() && !socket.isClosed()) {
        return;
      }
      socket = new Socket();
      socket.connect(new java.net.InetSocketAddress(host, port), 5000); // 5s timeout
      outputStream = new ObjectOutputStream(socket.getOutputStream());
      inputStream = new ObjectInputStream(socket.getInputStream());

      startListening();
    }
  }

  private void startListening() {
    // chỉ 1 thread ngồi chờ sever trả giữ liệu
    // nhiều rq nhưng 1 thread đợi kết quả
    if (listening) {
      return;
    }
    listening = true;
    receiveExecutor.submit(
        () -> {
          try {
            while (socket != null && !socket.isClosed()) {
              Object incoming = inputStream.readObject();
              if (incoming instanceof Response<?> response) {
                if (response.getRequestId() == null) {
                  for (Consumer<Response<?>> listener : pushListeners) {
                    try {
                      listener.accept(response);
                    } catch (RuntimeException listenerError) {
                      // Lỗi nội bộ của listener — không hiện lên UI để tránh spam
                      listenerError.printStackTrace();
                    }
                  }
                  continue;
                }
                Consumer<Response<?>> callback = callbacks.remove(response.getRequestId());
                if (callback != null) {
                  try {
                    callback.accept(response);
                  } catch (RuntimeException callbackError) {
                      // Lỗi nội bộ của callback — không hiện lên UI để tránh spam
                      callbackError.printStackTrace();
                  }
                }
              }
            }
          } catch (EOFException e) {

          } catch (IOException | ClassNotFoundException e) {
            if (!isShuttingDown) {
                NotificationManager.showError("Connection error: " + e.getMessage());
            }
          } finally {
            listening = false;
            closeConnection();
            failCallbacks("Connection closed.");
            if (onConnectionLost != null && !isShuttingDown) {
                Platform.runLater(onConnectionLost);
            }
          }
        });
  }

  public void shutdown() {
    isShuttingDown = true;
    closeConnection();
    receiveExecutor.shutdownNow();
    sendExecutor.shutdownNow();
  }

  private void closeConnection() {
    // A4: Giữ cùng lock với send() để tránh race condition khi đóng stream
    synchronized (socketLock) {
      try {
        if (inputStream != null) {
          inputStream.close();
        }
      } catch (IOException ignored) {
      }
      try {
        if (outputStream != null) {
          outputStream.close();
        }
      } catch (IOException ignored) {
      }
      try {
        if (socket != null) {
          socket.close();
        }
      } catch (IOException ignored) {
      }

      inputStream = null;
      outputStream = null;
      socket = null;
    }
  }



  private void failCallbacks(String reason) {
    callbacks.values().forEach(cb -> 
      javafx.application.Platform.runLater(() -> cb.accept(Response.fail(reason)))
    );
    callbacks.clear();
  }
}
