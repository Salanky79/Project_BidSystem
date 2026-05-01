# Assignment: Auction System (Online Bidding System)

## Goal

Xây dựng hệ thống đấu giá trực tuyến bằng Java, áp dụng OOP + networking + design pattern.

## Core Features (BẮT BUỘC)

### 1. User Management

- Đăng ký / đăng nhập
- Role:
  - Bidder: tham gia đấu giá
  - Seller: đăng sản phẩm
  - Admin: quản lý hệ thống

### 2. Auction Item Management

- CRUD sản phẩm đấu giá
- Thuộc tính:
  - tên, mô tả, giá khởi điểm
  - giá hiện tại
  - thời gian bắt đầu / kết thúc

### 3. Bidding System

- Người dùng đặt giá > giá hiện tại
- Validate bid hợp lệ
- Cập nhật người dẫn đầu

### 4. Auction Lifecycle

- Tự động đóng khi hết thời gian
- Xác định winner
- Trạng thái:
  OPEN → RUNNING → FINISHED → PAID/CANCELED

### 5. Error Handling

- Bid không hợp lệ
- Đấu giá khi đã đóng
- Lỗi hệ thống / kết nối

### 6. GUI

- JavaFX / Swing
- Màn hình:
  - danh sách auction
  - chi tiết sản phẩm
  - realtime bidding
  - quản lý sản phẩm

---

## Advanced Features (OPTIONAL)

- Auto-bidding (maxBid, increment)
- Concurrent bidding (không race condition, lost update)
- Anti-sniping (gia hạn thời gian khi bid cuối)
- Realtime update (Observer + Socket)
- Bid history chart (line chart realtime)

---

## Architecture Requirements

- Client – Server
- Communication: Socket / REST (JSON)
- Client: MVC (JavaFX)
- Server: Controller → Service → DAO
- Chỉ server truy cập database

---

## OOP Requirements

- Entity, User, Item, Auction, BidTransaction
- Inheritance (User → Bidder/Seller/Admin)
- Encapsulation, Polymorphism, Abstraction

---

## Design Patterns

- Singleton
- Factory Method
- Observer
- (Optional: Strategy / Command)

---

## Technical Requirements

- Build: Maven / Gradle
- Unit test: JUnit
- Git commit thường xuyên
- Code clean + theo convention

---

## Evaluation Criteria

- OOP + design pattern
- Chức năng đầy đủ
- Concurrency đúng
- Realtime update
- Kiến trúc rõ ràng
- Code quality + test + CI/CD
