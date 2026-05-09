package com.auction.client.network;

import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RestClient {

    /** Phải trùng với {@code ServerApplication} (Javalin). */
    private static final String API_BASE = "http://localhost:8080";

    private static final Gson gson = new Gson();

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
                JsonObject body = new JsonObject();
                body.addProperty("username", username);
                body.addProperty("password", password);
                String jsonBody = gson.toJson(body);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE + "/api/auth/login"))
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
                    String detail = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                    JsonObject err = new JsonObject();
                    err.addProperty("success", false);
                    err.addProperty("message", detail);
                    err.add("data", JsonNull.INSTANCE);
                    onResponse.accept(gson.toJson(err));
                }
            }
        });
    }
}
