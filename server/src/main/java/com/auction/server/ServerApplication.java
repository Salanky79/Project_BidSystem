package com.auction.server;

import com.auction.server.controller.RequestHandler;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerApplication {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) { // kết nối máy chủ với cổng 8080
            System.out.println("Server dau gia dang chay tai cong 8080...");

            while (true) {
                Socket clientSocket = serverSocket.accept(); //wait for client
                // Mỗi khách hàng là một Thread mới
                new Thread(new RequestHandler(clientSocket)).start(); // truyền vào runnable
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
