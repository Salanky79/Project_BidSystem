package com.auction.client.network;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RestClient {
    private static RestClient instance;
    private HttpClient httpClient;
    private ExecutorService executorService;

    private RestClient() {
        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    public static synchronized RestClient getInstance() {
        if (instance == null) {
            instance = new RestClient();
        }
        return instance;
    }

    public void login(String username, String password, Consumer<String> onResponse) {
        executorService.submit(() -> {
            try {
                // Tạo chuỗi JSON đơn giản
                String jsonBody = "{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8081/api/login"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (onResponse != null) {
                    onResponse.accept(response.body());
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (onResponse != null) {
                    onResponse.accept("{\"status\":\"ERROR\", \"message\":\"Lỗi kết nối REST API: " + e.toString() + "\"}");
                }
            }
        });
    }
}
