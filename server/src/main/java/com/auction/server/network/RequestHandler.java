package com.auction.server.network;

import com.auction.server.controllers.AuctionManager;
import com.auction.share.models.user.User;
import java.io.*;
import java.net.Socket;

public class RequestHandler implements Runnable {
    private Socket socket;
    private User currentUser;
    // Lấy ông chủ duy nhất ở đây
    private AuctionManager manager = AuctionManager.getInstance();

    public RequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // Khai báo ngoài try để finally có thể dùng được
        PrintWriter out = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // ĐĂNG KÝ NGƯỜI NGHE
            manager.addObserver(out);

            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split("\\|");
                String cmd = parts[0];

                if (cmd.equals("LOGIN")) {
                    currentUser = manager.login(parts[1], parts[2]);
                    if (currentUser != null) out.println("SUCCESS|Chao " + currentUser.getUsername());
                    else out.println("FAIL|Sai tai khoan");
                }
                else if (cmd.equals("LIST")) {
                    out.println(manager.listItems());
                }
            }
        } catch (IOException e) {
            System.out.println("Mot khach hang da ngat ket noi.");
        } finally {
            // Kiểm tra nếu out khác null thì mới xóa
            if (out != null) {
                manager.removeObserver(out);
            }
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }
}