package com.auction.server;

import com.auction.server.dao.UserDAO;
import com.auction.server.controller.RequestHandler;
import com.auction.server.network.AuctionServer;
import com.auction.server.service.UserService;

import java.io.IOException;

public class ServerApplication {

    public static void main(String[] args) {
        UserDAO userDao = new UserDAO();
        UserService userService = new UserService(userDao);
        RequestHandler requestHandler = new RequestHandler(userService);
        AuctionServer auctionServer = new AuctionServer(8080, requestHandler);

        try {
            auctionServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
