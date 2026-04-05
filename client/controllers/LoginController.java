package controllers; // Chú ý: Đổi lại đúng package của bạn (ví dụ: com.auction.client.controller)

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;

public class LoginController {

    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            // 1. Tải file Home.fxml
            // Lưu ý: Đường dẫn phải chính xác với cấu trúc thư mục resources của bạn
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/Home.fxml"));
            Parent homeRoot = loader.load();

            // 2. Lấy Stage hiện tại (cửa sổ đang hiển thị)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 3. Tạo Scene mới với nội dung là Home.fxml
            Scene homeScene = new Scene(homeRoot);

            // 4. Đặt Scene mới lên Stage và hiển thị
            stage.setScene(homeScene);
            stage.centerOnScreen(); // Căn giữa lại màn hình vì Home thường to hơn Login
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Không tìm thấy file Home.fxml hoặc lỗi nạp file!");
        }
    }
}