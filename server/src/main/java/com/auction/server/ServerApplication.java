package com.auction.server;

import com.auction.server.network.LoginHttpHandler;
import com.auction.server.network.RequestHandler;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class ServerApplication {
    public static void main(String[] args) {
        try {
            // 1. Khởi động REST API Server (Port 8081)
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(8081), 0);
            httpServer.createContext("/api/login", new LoginHttpHandler());
            httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            httpServer.start();
            System.out.println("REST API Server dang chay tai cong 8081...");

            // 2. Khởi động TCP Socket Server (Port 8080)
            try (ServerSocket serverSocket = new ServerSocket(8080)) {
                System.out.println("TCP Socket Server dau gia dang chay tai cong 8080...");
                while (true) {
                    Socket clientSocket = serverSocket.accept(); // wait for client
                    // Mỗi khách hàng là một Thread mới
                    new Thread(new RequestHandler(clientSocket)).start(); // truyền vào runnable
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}