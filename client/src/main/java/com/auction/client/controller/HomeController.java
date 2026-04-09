package controllers; // Đảm bảo package này khớp với vị trí file Java

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

// Kéo cái lưới trống từ giao diện FXML vào đây để điều khiển
    @FXML
    private GridPane auctionGrid;

    @FXML
    public void initialize() {
        // 1. Tạo một mảng dữ liệu giả lập (Sau này bạn sẽ lấy List<Item> từ Server trả về)
        String[] itemNames = {"iPhone 15 Pro", "Đồng hồ Rolex", "Túi xách Chanel", "Siêu xe Porsche", "Tranh Van Gogh", "Nhẫn kim cương"};
        String[] categories = {"Electronic", "Watch", "Hand Bag", "Car", "Fine Art", "Jewelry"};
        String[] icons = {"📱", "⌚", "👜", "🚗", "🖼", "💍"};
        double[] prices = {1200.0, 5500.0, 3200.0, 150000.0, 85000.0, 12000.0};

        // Biến để tính toán tọa độ (Cột và Hàng) nhét vào lưới
        int column = 0;
        int row = 0;

        try {
            // 2. Vòng lặp để đẻ ra 6 cái thẻ sản phẩm
            for (int i = 0; i < itemNames.length; i++) {

                // Lấy cái "Khuôn đúc" ItemCard.fxml ra
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ItemCard.fxml"));
                HBox card = loader.load(); // Vì thẻ của bạn bọc ngoài bằng HBox

                // Lấy "Bác thợ" điều khiển khuôn đúc để đắp dữ liệu vào
                ItemCardController cardController = loader.getController();
                cardController.setData(icons[i], categories[i], itemNames[i], prices[i], 0, "12 D 05 Hrs");

                // --- Thuật toán xếp gạch vào lưới 2 cột ---
                if (column == 2) { // Nếu đã xếp đủ 2 cột (0 và 1) thì xuống dòng
                    column = 0;
                    row++;
                }

                // Nhét cái thẻ vừa nặn xong vào đúng vị trí Cột và Hàng trên lưới
                auctionGrid.add(card, column++, row);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi không tìm thấy file ItemCard.fxml. Hãy kiểm tra lại đường dẫn!");
        }
    }
    public void handleHome() {}
    public void handleLogout(javafx.event.ActionEvent event) {
        System.out.println("Đang đăng xuất... Trở về màn hình Login!");
        try {
            // 1. Tải lại file giao diện Login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            Parent loginRoot = loader.load();

            // 2. Lấy cái cửa sổ (Stage) hiện tại đang hiển thị
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            // 3. Đổi cảnh sang Login
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi: Không tìm thấy file Login.fxml!");
        }
    }

    public void handleCardClick(javafx.scene.input.MouseEvent mouseEvent) {
            try {
                // 1. Nạp file FXML chi tiết
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AuctionDetail.fxml"));
                Parent root = loader.load();

                // 2. Tạo một Stage mới (Cửa sổ mới)
                Stage detailStage = new Stage();
                detailStage.setTitle("Chi tiết sản phẩm đấu giá");

                // 3. Cấu hình để nó là cửa sổ con (không cho bấm ra ngoài khi chưa đóng)
                detailStage.initModality(Modality.APPLICATION_MODAL);
                // Nếu muốn làm giao diện popup đẹp không có thanh tiêu đề Windows:
                // detailStage.initStyle(StageStyle.TRANSPARENT);

                Scene scene = new Scene(root);
                detailStage.setScene(scene);
                detailStage.show();

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Lỗi: Không tìm thấy file AuctionDetail.fxml. Kiểm tra lại đường dẫn!");
            }
        }

    }