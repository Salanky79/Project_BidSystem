package com.auction.server.network;

import com.auction.server.service.AuctionManager;
import com.auction.share.exceptions.AuthenticationException;
import com.auction.share.models.user.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class LoginHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }

            String json = requestBody.toString();
            String username = extractJsonValue(json, "username");
            String password = extractJsonValue(json, "password");

            String response;
            int statusCode;

            try {
                User user = AuctionManager.getInstance().login(username, password);
                response = "{\"status\":\"SUCCESS\", \"message\":\"Chào " + user.getUsername() + "\", \"username\":\"" + user.getUsername() + "\"}";
                statusCode = 200;
            } catch (AuthenticationException e) {
                response = "{\"status\":\"FAIL\", \"message\":\"" + e.getMessage() + "\"}";
                statusCode = 401; // Unauthorized
            } catch (Exception e) {
                response = "{\"status\":\"ERROR\", \"message\":\"" + e.getMessage() + "\"}";
                statusCode = 500;
            }

            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

    // Trích xuất value từ JSON String đơn giản (không cần dùng thư viện)
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return "";
        
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return "";
        
        int startQuote = json.indexOf("\"", colonIndex);
        if (startQuote == -1) return "";
        
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote == -1) return "";
        
        return json.substring(startQuote + 1, endQuote);
    }
}
