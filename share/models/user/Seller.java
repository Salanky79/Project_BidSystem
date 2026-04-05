package models.user;

import models.auction.Auction;
import models.item.Item;
import Enum.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Seller extends User {
    private double balance;
    private List<String> auctionIdList; // Danh sách id các phiên đấu giá người này đã tạo


    public Seller(String username, String password, String fullName, String uid) {
        super(username, password, fullName, uid); // Đẩy việc khởi tạo lên cho class User
        this.auctionIdList = new ArrayList<>();
        this.balance = 0.0;
        this.setRole(Role.SELLER);
    }

    // Trả về đối tượng Auction để Server còn lấy thông tin xử lý tiếp
    public Auction createAuction(Item item, LocalDateTime startTime, LocalDateTime endTime) {

        // Gọi đúng thứ tự: item trước, seller (chính là 'this') sau
        Auction newAuction = new Auction(item, this, startTime, endTime);

        this.auctionIdList.add(newAuction.getId());
        return newAuction;

    }

    // Truyền vào phiên đấu giá cụ thể muốn hủy
    public void cancelAuction(Auction auction) {
        if (this.auctionIdList.contains(auction.getId())) {
            // (Sau này bạn có thể gọi auction.setStatus("CANCELLED") ở đây)
            this.auctionIdList.remove(auction.getId());
            System.out.println("Đã hủy phiên đấu giá mặt hàng: " + auction.getItem().getName());
        } else {
            System.out.println("Lỗi: Phiên đấu giá này không thuộc về bạn!");
        }
    }

    public void addBalance(double amount) {
        if (amount > 0) {
            this.balance += amount;
            System.out.println("[$] Biến động số dư: Seller [" + this.getUsername() + "] được cộng +" + amount + "$. Số dư hiện tại: " + this.balance + "$");
        } else {
            System.out.println("Lỗi: Số tiền cộng vào phải lớn hơn 0!");
        }
    }

    public double getBalance() { return balance; }
}