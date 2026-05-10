package com.auction.server.network;

import com.auction.server.controller.RequestHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionServer {
    private final int port;
    private final RequestHandler requestHandler;
    private final ExecutorService clientExecutor;

    public AuctionServer(int port, RequestHandler requestHandler) {
        this.port = port;
        this.requestHandler = requestHandler;
        this.clientExecutor = Executors.newCachedThreadPool();
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("AuctionServer started on port " + port);
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                clientExecutor.submit(new ClientHandler(clientSocket, requestHandler));
            }
        } finally {
            clientExecutor.shutdown();
        }
    }
}
