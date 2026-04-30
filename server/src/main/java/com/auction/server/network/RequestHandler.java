package com.auction.server.network;

import com.auction.server.controllers.AuctionManager;
import com.auction.server.exceptions.*;
import com.auction.share.exceptions.*;
import com.auction.share.models.user.User;
import java.io.*;
import java.net.Socket;

public class RequestHandler implements Runnable {
    private Socket socket;
    private User currentUser;

    // Singleton
    private AuctionManager manager = AuctionManager.getInstance();

    public RequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        PrintWriter out = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            manager.addObserver(out);

            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split("\\|");
                String cmd = parts[0];

                try {
                    if (cmd.equals("LOGIN")) {
                        currentUser = manager.login(parts[1], parts[2]);
                        out.println("SUCCESS|Chao " + currentUser.getUsername());
                    }
                    else if (cmd.equals("LIST")) {
                        out.println(manager.listItems());
                    }
                    else if (cmd.equals("BID")) {
                        String result = manager.placeBid(parts[1], Double.parseDouble(parts[2]), currentUser);
                        out.println(result);
                    }
                } catch (AuctionSystemException e) {
                    out.println("FAIL|" + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Một khách hàng đã ngắt kết nối.");
        } finally {
            if (out != null) {
                manager.removeObserver(out);
            }
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }
}