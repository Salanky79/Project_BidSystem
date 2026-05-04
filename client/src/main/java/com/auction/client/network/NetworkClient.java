package com.auction.client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class NetworkClient {
    private static NetworkClient instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
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
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Đã kết nối tới Server thành công.");
        } catch (IOException e) {
            System.err.println("Không thể kết nối tới Server: " + e.getMessage());
        }
    }

    /**
     * Gửi một request tới server và gọi callback khi có kết quả trả về.
     * Hàm này chạy trên một Virtual Thread độc lập để không chặn UI Thread.
     */
    public void sendRequest(String request, Consumer<String> onResponse) {
        executorService.submit(() -> {
            try {
                if (out != null && in != null) {
                    out.println(request);
                    String response = in.readLine();
                    if (onResponse != null) {
                        onResponse.accept(response);
                    }
                } else {
                    if (onResponse != null) {
                        onResponse.accept("FAIL|Chưa kết nối tới Server.");
                    }
                }
            } catch (IOException e) {
                if (onResponse != null) {
                    onResponse.accept("FAIL|Lỗi khi đọc dữ liệu từ Server: " + e.getMessage());
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