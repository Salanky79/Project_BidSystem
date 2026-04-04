package src.models.user;

import src.models.auction.Auction;
import src.models.item.Item;
import Enum.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Seller extends User {
    private double balance;
    private List<Auction> auctionList; // Danh sách các phiên đấu giá người này đã tạo


    public Seller(String username, String password, String fullName, String uid) {
        super(username, password, fullName, uid); // Đẩy việc khởi tạo lên cho class User
        this.auctionList = new ArrayList<>();
        this.balance = 0.0;
        this.setRole(Role.SELLER);
    }

    // Trả về đối tượng Auction để Server còn lấy thông tin xử lý tiếp
    public Auction createAuction(Item item, LocalDateTime startTime, LocalDateTime endTime) {

        // Gọi đúng thứ tự: item trước, seller (chính là 'this') sau
        Auction newAuction = new Auction(item, this, startTime, endTime);

        this.auctionList.add(newAuction);
        return newAuction;

    }

    // Truyền vào phiên đấu giá cụ thể muốn hủy
    public void cancelAuction(Auction auction) {
        if (this.auctionList.contains(auction)) {
            // (Sau này bạn có thể gọi auction.setStatus("CANCELLED") ở đây)
            this.auctionList.remove(auction);
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

    // Getter cho danh sách
    public List<Auction> getAuctionList() { return auctionList; }
    public double getBalance() { return balance; }
}