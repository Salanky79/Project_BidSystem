package com.auction.client.network;

import com.auction.share.DTO.Request;
import com.auction.share.DTO.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class NetworkClient {
    private static NetworkClient instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ExecutorService executorService;

    private NetworkClient() {
        // Sử dụng Virtual Threads (Java 21+) để tiết kiệm tài nguyên
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        connect();
    }

    public static synchronized NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    private void connect() {
        try {
            socket = new Socket("localhost", 8080);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Đã kết nối tới Server thành công.");
        } catch (IOException e) {
            System.err.println("Không thể kết nối tới Server: " + e.getMessage());
        }
    }

    /**
     * Gửi một request tới server và gọi callback khi có kết quả trả về.
     * Hàm này chạy trên một Virtual Thread độc lập để không chặn UI Thread.
     */
    public void sendRequest(Request request, Consumer<Response<?>> onResponse) {
        executorService.submit(() -> {
            try {
                if (out != null && in != null) {
                    out.writeObject(request);
                    out.flush();
                    Object payload = in.readObject();
                    if (payload instanceof Response<?> response) {
                        if (onResponse != null) {
                            onResponse.accept(response);
                        }
                    } else if (onResponse != null) {
                        onResponse.accept(Response.fail("Phản hồi không hợp lệ từ Server."));
                    }
                } else if (onResponse != null) {
                    onResponse.accept(Response.fail("Chưa kết nối tới Server."));
                }
            } catch (IOException | ClassNotFoundException e) {
                if (onResponse != null) {
                    onResponse.accept(Response.fail("Lỗi khi đọc dữ liệu từ Server: " + e.getMessage()));
                }
            }
        });
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            executorService.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}