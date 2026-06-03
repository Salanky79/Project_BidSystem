# Hệ Thống Đấu Giá Trực Tuyến (Auction System)

## 1. Mô tả ngắn gọn bài toán và phạm vi hệ thống
Hệ thống đấu giá trực tuyến là một ứng dụng Client-Server cho phép người dùng tham gia đấu giá các sản phẩm qua mạng theo thời gian thực (real-time). 
- **Phạm vi hệ thống:** 
  - **Server:** Đóng vai trò trung tâm, quản lý kết nối từ nhiều Client sử dụng Socket TCP/IP và xử lý đa luồng (Multi-threading). Server chịu trách nhiệm xử lý toàn bộ logic nghiệp vụ (đăng ký, đăng nhập, đặt giá, tự động đặt giá, thiết lập bước giá), duy trì trạng thái của các phiên đấu giá (chờ xử lý, đang diễn ra, đã kết thúc) và thao tác với cơ sở dữ liệu MySQL. Server cũng đóng vai trò Broadcast (phát sóng) các thay đổi trạng thái (như có người đặt giá mới) đến tất cả các Client đang theo dõi.
  - **Client:** Là ứng dụng Desktop cung cấp giao diện đồ họa (GUI) phát triển bằng JavaFX. Phân tách rõ ràng giao diện của người mua (Bidder) và người bán (Seller). Cho phép người dùng đăng ký, đăng nhập, quản lý hồ sơ cá nhân, xem danh sách sản phẩm, tạo phiên đấu giá mới, và thực hiện đặt giá/đặt giá tự động.

## 2. Công nghệ sử dụng, môi trường chạy và yêu cầu cài đặt
### Công nghệ sử dụng:
- **Ngôn ngữ lập trình:** Java (Tối thiểu JDK 17, đề xuất JDK 21).
- **Công cụ quản lý dự án & Build:** Maven.
- **Kiến trúc mạng:** Java Socket (TCP) truyền tải dữ liệu dưới dạng JSON.
- **Giao diện người dùng (Client):** JavaFX (Sử dụng Scene Builder, file `.fxml` và kiến trúc MVC).
- **Cơ sở dữ liệu:** MySQL.
- **Thư viện bên thứ 3 (Third-party libraries):**
  - `HikariCP`: Quản lý Connection Pool giúp tối ưu hóa hiệu suất kết nối cơ sở dữ liệu khi có nhiều truy vấn đồng thời.
  - `jBCrypt`: Băm (hash) và kiểm tra mật khẩu an toàn.
  - `Cloudinary`: Tích hợp API lưu trữ và quản lý hình ảnh sản phẩm đấu giá trực tuyến.
  - `dotenv-java`: Quản lý linh hoạt các biến môi trường cấu hình (Database, Cloudinary...).
  - `slf4j`: Ghi log (logging) hoạt động của hệ thống.

### Yêu cầu cài đặt & Môi trường chạy:
- Cài đặt **JDK 21** (hoặc tối thiểu JDK 17).
- Cài đặt **Apache Maven** (nếu không sử dụng Maven wrapper `mvnw` đi kèm).
- Cài đặt **MySQL Server** (phiên bản 8.0+).
- **Cấu hình môi trường:** Tạo cơ sở dữ liệu MySQL và tạo file `.env` tại thư mục `server` (cùng cấp với `pom.xml` của server) để thiết lập thông tin: DB_URL, DB_USER, DB_PASSWORD, CLOUDINARY_URL...

## 3. Cấu trúc thư mục chính
Dự án áp dụng mô hình phân tách Module rõ ràng thông qua Maven:
```
Project_BidSystem/
│
├── client/      # Mã nguồn giao diện (JavaFX) và xử lý kết nối phía Client.
│   ├── src/main/java/com/auction/client/
│   │   ├── controller/   # Điều khiển giao diện (LoginController, HomeController, SellerDashboard...)
│   │   ├── network/      # Xử lý Socket kết nối tới Server, lắng nghe Broadcast
│   │   ├── service/      # Gọi logic API (gửi Request/nhận Response)
│   │   └── view/         # (Trong thư mục resources) Chứa các file FXML thiết kế UI
│   └── pom.xml
│
├── server/      # Mã nguồn Server (Core logic).
│   ├── src/main/java/com/auction/server/
│   │   ├── controller/   # Chuyển hướng Request (AuctionController, UserController...)
│   │   ├── dao/          # Truy xuất Database (Data Access Object)
│   │   ├── network/      # Quản lý ServerSocket, Thread Pool, Worker cho mỗi Client
│   │   └── service/      # Xử lý nghiệp vụ chính (Bidding, AutoBid, Broadcast...)
│   └── pom.xml
│
├── share/       # Thư viện dùng chung (Common).
│   └── src/main/java/com/auction/share/
│       ├── DTO/          # Data Transfer Object (Request/Response trao đổi giữa Client-Server)
│       ├── enums/        # Các Enum chung (Role, AuctionStatus, Category...)
│       └── models/       # Entity (User, Bidder, Seller, Auction, Bid...)
│
└── pom.xml      # POM gốc (Parent) quản lý chung các modules và dependencies.
```

## 4. Câu lệnh dòng lệnh để chạy chương trình
Từ thư mục gốc của dự án (nơi chứa file `pom.xml` Parent), mở Terminal/Command Prompt và thực hiện lệnh build:

```bash
# Build toàn bộ project ra file thực thi (.jar)
mvn clean package
```
*(Nếu hệ thống chưa cài Maven toàn cục, có thể dùng `./mvnw clean package` trên Linux/MacOS hoặc `mvnw.cmd clean package` trên Windows).*

Sau quá trình build thành công, Maven sẽ đóng gói toàn bộ chương trình và các thư viện liên quan thành một file "fat-jar" (`jar-with-dependencies`) nằm trong thư mục `target` của mỗi module `client` và `server`.

## 5. Hướng dẫn chạy Server/Client theo thứ tự

**Bước 1: Khởi động Server (Bắt buộc chạy trước)**

> **[LƯU Ý QUAN TRỌNG]** Trước khi khởi động Server, bạn **BẮT BUỘC** phải tạo/sao chép file `.env` vào thư mục `server` (chứa các biến môi trường cấu hình DB, Cloudinary). Nếu không có file này, Server sẽ báo lỗi và không thể chạy.

Mở một cửa sổ Terminal mới, di chuyển vào thư mục module `server` và khởi chạy file jar:
```bash
cd source/server
java -jar target/server-1.0-SNAPSHOT-jar-with-dependencies.jar
```
*(Server sẽ đọc file `.env`, khởi tạo Connection Pool với Database và mở cổng lắng nghe (Port mặc định 8080) chờ Client kết nối).*

**Bước 2: Khởi động Client (Chạy sau khi Server đã báo Ready)**
Mở một cửa sổ Terminal khác, di chuyển vào thư mục module `client` và khởi chạy ứng dụng:
```bash
cd source/client
java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar
```
*(Hoặc dùng lệnh `mvn javafx:run` trong thư mục client nếu muốn khởi chạy trực tiếp qua plugin của Maven).*

## 6. Danh sách chức năng đã hoàn thành
Hệ thống đã triển khai thành công các nhóm chức năng chính yếu yếu sau:

**Hệ thống nền tảng & Bảo mật:**
- [x] Kiến trúc Client-Server với kết nối TCP Socket liên tục.
- [x] Xử lý đa luồng (Multi-threading) bằng Thread Pool tại Server, cho phép nhiều người dùng thao tác đồng thời.
- [x] Quản lý cấu hình bằng biến môi trường (Dotenv) và Connection Pool (HikariCP).
- [x] Băm mật khẩu người dùng lưu trữ trong Database (jBCrypt).
- [x] Upload và quản lý ảnh sản phẩm lên Cloud (Cloudinary API).
- [x] Hệ thống Broadcast (phát sóng) cập nhật dữ liệu Real-time tới các Client.

**Tài khoản người dùng:**
- [x] Phân quyền người dùng: Người mua (Bidder) và Người bán (Seller).
- [x] Đăng ký / Đăng nhập tài khoản an toàn.
- [x] Xem và cập nhật hồ sơ cá nhân (Update Profile).

**Chức năng Người bán (Seller):**
- [x] Bảng điều khiển riêng biệt (Seller Dashboard) quản lý sản phẩm.
- [x] Tạo phiên đấu giá mới (Thêm sản phẩm, hẹn thời gian bắt đầu/kết thúc, giá khởi điểm, upload ảnh).
- [x] Bộ lọc trạng thái phiên đấu giá (Tất cả, Đang hoạt động, Đã kết thúc).
- [x] Cài đặt/Thay đổi bước giá (Bid Step) cho sản phẩm đang đấu giá.
- [x] Hủy phiên đấu giá.

**Chức năng Người mua (Bidder):**
- [x] Xem danh sách các sản phẩm đang được đấu giá ở màn hình Home.
- [x] Xem chi tiết phiên đấu giá (Giá hiện tại, thời gian còn lại, lịch sử đặt giá).
- [x] **Đặt giá thủ công (Place Bid):** Đặt giá ngay lập tức với số tiền hợp lệ, Client sẽ tự động cập nhật số tiền nhảy real-time nếu có người khác đặt.
- [x] **Đặt giá tự động (Auto-bid):** Cấu hình tính năng đấu giá tự động (thiết lập mức giá tối đa và bước nhảy), Server sẽ tự động thay mặt Bidder đặt giá nếu bị người khác vượt qua (tính năng AutoBid Service chạy ngầm).
- [x] Hủy đăng ký đấu giá tự động (Cancel Auto-bid).

## 7. Link báo cáo PDF và video demo
- **Báo cáo PDF:** [Xem Báo cáo (Google Drive)](https://drive.google.com/file/d/18QEeqrBY4U3qRsXKBS0FSHK6CMW-OjAN/view?usp=sharing)
- **Video Demo:** [Link Youtube / Google Drive video chạy thử ứng dụng](https://drive.google.com/file/d/1dLONG-eXajRUBfRDilTJNgHQ2jw76SAm/view?usp=sharing)
