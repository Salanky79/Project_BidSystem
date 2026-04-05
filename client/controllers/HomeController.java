package controllers; // Đảm bảo package này khớp với vị trí file Java

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.event.MouseEvent;
import java.io.IOException;

public class HomeController {
    // Tạm thời để trống để chạy test giao diện
    @FXML
    public void initialize() {
        System.out.println("Đã vào màn hình Home thành công!");
    }
    public void handleHome() {}
    public void handleLogout() {}

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