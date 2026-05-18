package com.auction.server.service;

import com.auction.share.exceptions.AuctionSystemException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;
import com.auction.share.enums.AuctionStatus;
import com.auction.share.exceptions.AuthenticationException;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionManager {
    private static AuctionManager instance;

    // CHỈ QUẢN LÝ PHIÊN ĐẤU GIÁ (AUCTIONS)
    private List<Auction> auctions = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<PrintWriter> observers = new ArrayList<>();

    private AuctionManager() {
        // 1. Tạo dữ liệu mẫu: Người bán, Người mua và Món hàng
        Seller dummySeller = new Seller(
                "seller1",
                "123",
                "Người Bán",
                "0900000000",
                "seller1@example.com"
        );
        Bidder dummyBidder = new Bidder(
                "admin",
                "123",
                "Admin",
                "0901111111",
                "admin@example.com",
                "Hanoi"
        );
        dummyBidder.deposit(10000.0); // Nạp tiền cho admin để test không bị lỗi thiếu tiền

        Item dummyItem = new Item("iPhone 15", "Like New", 1000.0, dummySeller.getId());

        // 2. Tạo phiên đấu giá cho cái iPhone đó (Bắt đầu từ quá khứ, kết thúc sau 1 tiếng)
        LocalDateTime start = LocalDateTime.now().minusMinutes(5); // Test
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        Auction dummyAuction = new Auction(dummyItem, dummySeller, start, end);

        // Mở cửa phiên đấu giá luôn để lát test đặt giá được ngay
        dummyAuction.startAuction();

        // 3. Thêm vào danh sách quản lý
        auctions.add(dummyAuction);
        users.add(dummyBidder);
        users.add(dummySeller);


        // --- TẠO ĐỒNG HỒ CHẠY NGẦM (Mục 3 & 5 Tuần 7) ---
        // tao thong bao sau moi 1s de cap nhap phien dau gia

        Thread timerThread = new Thread(() -> {
            while (true) {
                try {
                    LocalDateTime now = LocalDateTime.now();
                    for (Auction a : auctions) {
                        // Tự động MỞ
                        if (a.getStatus() == AuctionStatus.OPEN && !now.isBefore(a.getStartTime())) {
                            a.startAuction();
                            broadcast("HỆ THỐNG: Phien dau gia [" + a.getItem().getName() + "] DA BAT DAU!");
                        }

                        // Tự động ĐÓNG
                        if (a.getStatus() == AuctionStatus.RUNNING && now.isAfter(a.getEndTime())) {
                            a.closeAuction();
                            broadcast("HỆ THỐNG: Phien dau gia [" + a.getItem().getName() + "] DA KET THUC!");
                            if (a.getHighestBidder() != null) {
                                broadcast("CHUC MUNG: " + a.getHighestBidder().getUsername() + " da thang voi gia " + a.getCurrentHighestBid() + "$");
                            }
                        }
                    }
                    Thread.sleep(1000); // Ngủ 1 giây
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        timerThread.setDaemon(true); // luồng phụ
        timerThread.start();
        // --- KẾT THÚC ĐOẠN ĐỒNG HỒ ---
    }

    public static AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    // --- CÁC HÀM CỦA OBSERVER PATTERN ---
    public synchronized void addObserver(PrintWriter out) {
        observers.add(out);
        System.out.println("Thêm 1 observer. Tổng số: " + observers.size());
    }

    public synchronized void removeObserver(PrintWriter out) {
        observers.remove(out);
    }

    public synchronized void broadcast(String message) {
        List<PrintWriter> failedObservers = new ArrayList<>();
        for (PrintWriter writer : observers) {
            writer.println("NOTIFY|" + message);
            if (writer.checkError()) { // Kiểm tra xem "loa" có hỏng không
                failedObservers.add(writer);
            }
        }
        observers.removeAll(failedObservers); // Tự động dọn dẹp những người đã offline
    }

    // --- LOGIC XỬ LÝ ---

    // SỬA LẠI: Lặp qua danh sách auctions thay vì items
    public String listItems() {
        StringBuilder sb = new StringBuilder("LIST:"); // de dang thay doi, immutable

        for (Auction a : auctions) {
            // Lấy tên món hàng, giá cao nhất hiện tại và trạng thái
            sb.append(a.getItem().getName()).append("|")
                    .append(a.getCurrentHighestBid()).append("|")
                    .append(a.getStatus()).append(";");
        }
        return sb.toString();
    }

    // SỬA LẠI: Chuyển việc kiểm tra giá cho lớp Auction.java lo
    public String placeBid(String itemName, double amount, User user) throws AuctionSystemException {
        // Chỉ Bidder (người mua) mới được đặt giá
        if (!(user instanceof Bidder)) {
            return "FAIL|Chi co nguoi mua (Bidder) moi duoc dat gia!";
        }
        Bidder bidder = (Bidder) user;

        for (Auction auction : auctions) {
            if (auction.getItem().getName().equalsIgnoreCase(itemName)) {
                try {
                    // Giao việc xử lý nghiệp vụ cho file Auction của bạn
                    // Nó sẽ tự ktra thời gian, trạng thái, số dư tiền...
                    boolean success = auction.processBid(bidder, amount);

                    if (success) {
                        // Gọi loa phường báo cho cả làng
                        broadcast("BID_UPDATE|" + itemName + "|" + amount + "|" + bidder.getUsername());
                        return "SUCCESS|Ban da dat gia thanh cong!";
                    }
                } catch (RuntimeException e) {
                    // Nếu code Auction quăng lỗi (như chưa tới giờ, thiếu tiền...), ta bắt lấy và báo cho Client
                    return "FAIL|" + e.getMessage();
                }
            }
        }
        return "FAIL|Khong tim thay phien dau gia nao cho mon hang nay!";
    }

    public User login(String username, String password) throws AuthenticationException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new AuthenticationException("Username và password không được để trống.");
        }

        for (User user : users) {
            if (username.equals(user.getUsername()) && password.equals(user.getPassword())) {
                return user;
            }
        }

        throw new AuthenticationException("Tài khoản hoặc mật khẩu không đúng.");
    }
}

// observer => dăng nhập thành công => placeBid => timerThread => hủy kết nối