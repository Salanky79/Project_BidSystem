package com.auction.client.controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class SellerDashboardController {

    // --- Thống kê ---
    @FXML
    private Label lblTotalRevenue;
    @FXML
    private Label lblActiveAuctions;
    @FXML
    private Label lblCompletedSales;
    @FXML
    private Label lblAwaitingShipment;

    // --- Các nút điều hướng chính ---
    @FXML
    private Button btnCreateNewAuction;
    @FXML
    private Button btnLogOut;

    /**
     * Phương thức khởi tạo tự động gọi sau khi file FXML được load.
     * Bạn có thể dùng để set dữ liệu ban đầu từ Database.
     */
    @FXML
    public void initialize() {
        System.out.println("Auction Dashboard Controller initialized!");
        // Ví dụ: lblTotalRevenue.setText("$50,000.00");
    }

    // --- Xử lý sự kiện (Event Handlers) ---

    @FXML
    void handleCreateNewAuction(ActionEvent event) {
        System.out.println("Mở form tạo đấu giá mới...");
        // Logic chuyển màn hình hoặc mở Dialog tại đây
    }

    @FXML
    void handleLogOut(ActionEvent event) {
        System.out.println("Đang đăng xuất...");
        // Logic quay lại màn hình Login
    }

    @FXML
    void handleViewBids(ActionEvent event) {
        // Lấy thông tin từ button đã nhấn (ví dụ nút ở thẻ Kim Cương)
        Button clickedBtn = (Button) event.getSource();
        System.out.println("Xem danh sách bid của sản phẩm này.");
    }

    @FXML
    void handleEditAuction(ActionEvent event) {
        System.out.println("Chỉnh sửa thông tin đấu giá.");
    }

    @FXML
    void handleManageSystem(ActionEvent event) {
        System.out.println("Chuyển tới trang quản lý hệ thống.");
    }

    @FXML
    void handleCreateInvoice(ActionEvent event) {
        System.out.println("Đang tạo hóa đơn cho đơn hàng đã kết thúc.");
    }

    // --- Các phương thức bổ trợ cho Sidebar ---
    @FXML
    void handleMenuNavigation(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String menuName = btn.getText().trim();
        System.out.println("Chuyển hướng đến menu: " + menuName);
    }
}
