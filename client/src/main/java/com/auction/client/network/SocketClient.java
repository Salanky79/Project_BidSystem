package com.auction.client.network;

import com.auction.client.session.SessionManager;
import com.auction.share.DTO.Request;
import com.auction.share.DTO.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SocketClient {
    private final String host;
    private final int port;
    private final ExecutorService executorService;
    // requestId → callback
    // nhiều request gửi cùng lúc => Cần ID
    private final Map<String, Consumer<Response<?>>> callbacks;
    private final SessionManager sessionManager;

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private volatile boolean listening;

    public SocketClient(String host, int port, SessionManager sessionManager) {
        this.host = host;
        this.port = port;
        this.sessionManager = sessionManager;
        // mỗi task một virtual thread riêng
        this.executorService = Executors.newCachedThreadPool();
        this.callbacks = new ConcurrentHashMap<>();
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

        Request outboundRequest = enrichWithUserId(request);
        if (onResponse != null) {
            callbacks.put(outboundRequest.getRequestId(), onResponse);
        }

        // serialization Object để truyền qua sever
        try {
            outputStream.writeObject(outboundRequest);
            outputStream.flush();
        } catch (IOException e) {
            if (onResponse != null) {
                callbacks.remove(outboundRequest.getRequestId());
                onResponse.accept(Response.fail("Failed to send request: " + e.getMessage()));
            }
            closeConnection();
        }
    }

    private void ensureConnected() throws IOException {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            return;
        }

        socket = new Socket(host, port);
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
        startListening();
    }

    private void startListening() {
        // chỉ 1 thread ngồi chờ sever trả giữ liệu
        // nhiều rq nhưng 1 thread đợi kết quả
        if (listening) {
            return;
        }
        listening = true;
        executorService.submit(() -> {
            try {
                while (socket != null && !socket.isClosed()) {
                    Object incoming = inputStream.readObject();
                    if (incoming instanceof Response<?> response) {
                        if (response.getRequestId() == null) {
                            continue;
                        }
                        Consumer<Response<?>> callback = callbacks.remove(response.getRequestId());
                        if (callback != null) {
                            try {
                                callback.accept(response);
                            } catch (RuntimeException callbackError) {
                                System.err.println("Callback handling failed: " + callbackError.getMessage());
                                callbackError.printStackTrace();
                            }
                        }
                    }
                }
            } catch (EOFException ignored) {
                System.err.println("Server closed socket (EOF).");
                ignored.printStackTrace();
            } catch (IOException | ClassNotFoundException ignored) {
                System.err.println("Socket listener failed: " + ignored.getMessage());
                ignored.printStackTrace();
            } finally {
                listening = false;
                closeConnection();
                failCallbacks("Connection closed.");
            }
        });
    }

    private Request enrichWithUserId(Request request) {
        if (request == null) {
            return null;
        }
        String userId = sessionManager.getCurrentUserId();
        if (userId == null || userId.isBlank()) {
            return request;
        }
        return request.withUserId(userId);
    }

    private void closeConnection() {
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

    private void failCallbacks(String message) {

        Response<?> failResponse =
                Response.fail(message);

        for (Consumer<Response<?>> callback : callbacks.values()) {
            callback.accept(failResponse);
        }

        callbacks.clear();
    }
}
