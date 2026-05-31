package com.auction.client.network;

import com.auction.client.utils.NotificationManager;
import com.auction.share.DTO.Request;
import com.auction.share.DTO.Response;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SocketClient {

  /** D1: Interface để UI lắng nghe trạng thái kết nối */
  public interface ConnectionStateListener {
    void onDisconnected();
    void onReconnected();
  }

  private final String host;
  private final int port;
  private final ExecutorService executorService;
  private final Map<String, Consumer<Response<?>>> callbacks;
  private final List<Consumer<Response<?>>> pushListeners;
  private final List<ConnectionStateListener> stateListeners;

  private Socket socket;
  private ObjectOutputStream outputStream;
  private ObjectInputStream inputStream;
  private volatile boolean listening;
  private final java.util.concurrent.atomic.AtomicInteger reconnectAttempts = new java.util.concurrent.atomic.AtomicInteger(0);
  private final java.util.concurrent.atomic.AtomicBoolean reconnecting = new java.util.concurrent.atomic.AtomicBoolean(false);
  private static final int MAX_RECONNECT_ATTEMPTS = 5;

  private final Object socketLock = new Object();
  private final ScheduledExecutorService scheduler;
  // C1: Executor riêng cho I/O gửi — tránh block caller thread (JavaFX thread)
  private final ExecutorService sendExecutor;

  public SocketClient(String host, int port) {
    this.host = host;
    this.port = port;

    this.executorService =
        Executors.newSingleThreadExecutor(
            runnable -> {
              Thread thread = new Thread(runnable);
              thread.setDaemon(true);
              return thread;
            });
    this.callbacks = new ConcurrentHashMap<>();
    this.pushListeners = new CopyOnWriteArrayList<>();
    this.stateListeners = new CopyOnWriteArrayList<>();
    this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
      Thread thread = new Thread(runnable);
      thread.setDaemon(true);
      return thread;
    });
    this.sendExecutor = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "socket-send-thread");
      thread.setDaemon(true);
      return thread;
    });
  }

  public void addConnectionStateListener(ConnectionStateListener listener) {
    if (listener != null) stateListeners.add(listener);
  }

  public void removeConnectionStateListener(ConnectionStateListener listener) {
    if (listener != null) stateListeners.remove(listener);
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
    try {
      ensureConnected();
    } catch (IOException e) {
      if (onResponse != null) {
        onResponse.accept(Response.fail("Cannot connect to server: " + e.getMessage()));
      }
      return;
    }

    java.util.concurrent.ScheduledFuture<?> timeoutTask = null;
    if (onResponse != null) {
      callbacks.put(request.getRequestId(), onResponse);
      timeoutTask = scheduler.schedule(() -> {
        Consumer<Response<?>> cb = callbacks.remove(request.getRequestId());
        if (cb != null) {
          javafx.application.Platform.runLater(() -> cb.accept(Response.fail("Request timeout after 30 seconds")));
        }
      }, 30, TimeUnit.SECONDS);
    }

    final java.util.concurrent.ScheduledFuture<?> finalTimeoutTask = timeoutTask;

    // C1: Submit I/O vào sendExecutor — không block caller thread
    sendExecutor.submit(() -> {
      synchronized (socketLock) {
        try {
          outputStream.writeObject(request);
          outputStream.flush();
        } catch (IOException e) {
          if (finalTimeoutTask != null) {
              finalTimeoutTask.cancel(false);
          }
          if (onResponse != null) {
            callbacks.remove(request.getRequestId());
            javafx.application.Platform.runLater(() -> onResponse.accept(Response.fail("Failed to send request: " + e.getMessage())));
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
      // C2: Reset counter mỗi lần kết nối thành công (kể cả manual reconnect qua send())
      reconnectAttempts.set(0);
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
    executorService.submit(
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
            // Server đóng kết nối — onDisconnected() sẽ được gọi ở finally
          } catch (IOException | ClassNotFoundException e) {
            NotificationManager.showError("Lỗi kết nối: " + e.getMessage());
          } finally {
            listening = false;
            closeConnection();
            failCallbacks("Connection closed.");
            // D2: Notify UI về trạng thái mất kết nối
            stateListeners.forEach(l -> javafx.application.Platform.runLater(l::onDisconnected));
            // D2: Tự động thử kết nối lại sau 5 giây
            scheduleReconnect();
          }
        });
  }

  public void shutdown() {
    closeConnection();
    executorService.shutdownNow();
    sendExecutor.shutdownNow();
    if (scheduler != null) {
      scheduler.shutdownNow();
    }
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

  private void scheduleReconnect() {
    if (!reconnecting.compareAndSet(false, true)) return;
    int currentAttempts = reconnectAttempts.incrementAndGet();
    if (currentAttempts >= MAX_RECONNECT_ATTEMPTS) {
      reconnecting.set(false);
      NotificationManager.showError(
          "Không thể kết nối tới máy chủ sau " + MAX_RECONNECT_ATTEMPTS + " lần thử.");
      return;
    }
    long delaySeconds = currentAttempts * 3L;
    NotificationManager.showWarning(
        "Mất kết nối — thử lại lần " + currentAttempts + " sau " + delaySeconds + "s...");
    scheduler.schedule(() -> {
      try {
        ensureConnected();
        reconnectAttempts.set(0);
        reconnecting.set(false);
        NotificationManager.showSuccess("Đã kết nối lại với máy chủ.");
        stateListeners.forEach(l -> javafx.application.Platform.runLater(l::onReconnected));
      } catch (IOException e) {
        reconnecting.set(false);
        scheduleReconnect();
      }
    }, delaySeconds, TimeUnit.SECONDS);
  }

  private void failCallbacks(String reason) {
    callbacks.values().forEach(cb -> 
      javafx.application.Platform.runLater(() -> cb.accept(Response.fail(reason)))
    );
    callbacks.clear();
  }
}
