package src.models.user;

import src.models.auction.BidTransaction;
import Enum.Role;

import java.util.ArrayList;
import java.util.List;

public class Bidder extends User {
    private double balance;
    private String address;
    private List<BidTransaction> bidHistory; // Đã đổi sang List để dùng được .add()

    // Constructor 1: Đầy đủ thông tin
    public Bidder(String username, String password, String fullName, String uid, String address) {
        super(username, password, fullName, uid);
        this.address = address;
        this.balance = 0.0;
        this.bidHistory = new ArrayList<>(); // Bắt buộc phải khởi tạo List
        this.setRole(Role.BIDDER);
    }


    // Getters
    public String getName() { return this.getFullName(); } // Đảm bảo fullName để protected ở class User
    public double getBalance() { return balance; }

    // Rút tiền / Khấu trừ (Đã thêm void và check an toàn)
    public void deductBalance(double amount) {
        if (amount > balance) {
            throw new RuntimeException("Lỗi: Số dư không đủ để thanh toán!");
        }
        this.balance -= amount;
    }

    // Nạp tiền (Đổi thành chữ 'd' thường cho chuẩn quy tắc viết tên hàm Java)
    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    // Lưu lại lịch sử giao dịch CỦA CÁ NHÂN NGƯỜI NÀY
    public void recordBidHistory(BidTransaction transaction) {
        this.bidHistory.add(transaction);
    }
}