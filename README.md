auction-system/
├── pom.xml (hoặc build.gradle)                 // Quản lý thư viện chung cho toàn dự án
│
├── server/                                     // PHẦN BẠN SẼ LÀM TRONG GIAI ĐOẠN NÀY
│   ├── src/main/java/com/auction/server/
│   │   ├── ServerApplication.java              // File main() khởi chạy Server
│   │   │
│   │   ├── models/                             // Chứa các đối tượng OOP theo chuẩn
│   │   │   ├── core/                           // Lớp cơ sở (Entity)
│   │   │   ├── user/                           // User, Bidder, Seller, Admin
│   │   │   ├── item/                           // Item, Electronics, Art...
│   │   │   └── auction/                        // Auction, BidTransaction
│   │   │
│   │   ├── dao/                                // Database Access Object (Truy cập CSDL)
│   │   │   ├── UserDao.java                    // Lưu ý: Chỉ Server mới truy cập database
│   │   │   ├── ItemDao.java                    
│   │   │   └── AuctionDao.java
│   │   │
│   │   ├── controllers/                        // Xử lý logic nghiệp vụ đấu giá (MVC Server)
│   │   │   ├── UserController.java             // Logic đăng ký, đăng nhập (3.1.1)
│   │   │   ├── ItemController.java             // Logic thêm/sửa/xóa sản phẩm (3.1.2)
│   │   │   └── AuctionController.java          // Logic đặt giá, kết thúc phiên (3.1.3 & 3.1.4)
│   │   │
│   │   ├── network/                            // Xử lý giao tiếp mạng (Socket / REST API)
│   │   │   └── RequestHandler.java 
│   │   │
│   │   ├── exceptions/                         // Xử lý lỗi & ngoại lệ (3.1.5)
│   │   │   ├── InvalidBidException.java        // Lỗi: Đặt giá thấp hơn giá hiện tại
│   │   │   └── AuctionClosedException.java     // Lỗi: Đấu giá khi phiên đã đóng
│   │   │
│   │   └── utils/                              // Các hàm tiện ích (Format thời gian, check logic...)
│   │
│   └── src/test/java/com/auction/server/       // Chứa code JUnit Test
│       ├── controllers/
│       └── models/
│
└── client/                                     // PHẦN GIAO DIỆN (LÀM SAU KHI XONG LOGIC)
    ├── src/main/java/com/auction/client/
    │   ├── ClientApplication.java              // File main() khởi chạy Client (JavaFX)
    │   ├── controllers/                        // Controller của giao diện JavaFX
    │   └── network/                            // Gửi request lên Server
    │
    └── src/main/resources/
        └── views/                              // Chứa các file giao diện .fxml
