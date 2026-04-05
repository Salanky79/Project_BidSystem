# 🔨 Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A22?style=for-the-badge&logo=apache-maven&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-Client%20/%20Server-blue?style=for-the-badge)

Một hệ thống đấu giá mô hình Lập trình mạng (Client - Server) đa luồng, được thiết kế theo chuẩn hướng đối tượng (OOP) và đảm bảo tính toàn vẹn của dữ liệu giao dịch.

## 🌟 Tính năng nổi bật (Dự kiến)
* **Kiến trúc Client - Server:** Cho phép nhiều người dùng (Bidders) kết nối vào máy chủ cùng lúc để tham gia đấu giá thời gian thực.
* **Bảo mật giao dịch:** Sử dụng cấu trúc dữ liệu bất biến (Immutable Transactions) để lưu vết lịch sử đặt giá, chống gian lận.
* **Quản lý phiên đấu giá:** Tự động mở/đóng phiên đấu giá, tìm ra người chiến thắng dựa trên mức giá cao nhất hợp lệ.
* **Mở rộng linh hoạt:** Các đối tượng (Models) được thiết kế theo chuẩn Domain-Driven Design, dễ dàng nâng cấp sau này.

## 🏗️ Kiến trúc & Công nghệ
* **Ngôn ngữ:** Java (JDK 25)
* **Quản lý thư viện:** Maven
* **Mô hình mạng:** Java Socket / RMI (Multithreading)

## 📁 Cấu trúc Dự án
Dự án được chia thành các module độc lập. Hiện tại đang phát triển module `Server`:
```text
auction-system
 ┣ 📂 server
 ┃ ┗ 📂 src/main/java/com/auction/server
 ┃   ┣ 📂 models             # Chứa các thực thể cốt lõi
 ┃   ┃ ┣ 📂 auction          # Auction, BidTransaction, AuctionStatus...
 ┃   ┃ ┣ 📂 core             # Entity (Lớp cơ sở)
 ┃   ┃ ┣ 📂 item             # Item, Electronics...
 ┃   ┃ ┗ 📂 user             # User, Bidder, Seller...
 ┃   ┗ 📜 ServerApplication.java # Entry point khởi chạy máy chủ
 ┗ 📜 pom.xml                # Cấu hình Maven
