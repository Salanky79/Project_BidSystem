package com.auction.client.controller;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Interface cung cấp các phương thức mặc định (default methods) cho các JavaFX Controllers
 * để thực hiện việc chuyển đổi giao diện (views) một cách siêu đơn giản và tránh lặp mã.
 */
public interface ChangeView {

    /**
     * Trích xuất Stage hiện tại từ đối tượng ActionEvent.
     *
     * @param event sự kiện tương tác của người dùng
     * @return Stage hiện tại hoặc null nếu không tìm thấy
     */
    default Stage getStage(ActionEvent event) {
        if (event == null || event.getSource() == null) {
            return null;
        }
        if (event.getSource() instanceof Node node) {
            if (node.getScene() != null) {
                return (Stage) node.getScene().getWindow();
            }
        }
        return null;
    }

    /**
     * Trích xuất Stage hiện tại từ đối tượng Node.
     *
     * @param node đối tượng giao diện JavaFX
     * @return Stage hiện tại hoặc null nếu không tìm thấy
     */
    default Stage getStage(Node node) {
        if (node != null && node.getScene() != null) {
            return (Stage) node.getScene().getWindow();
        }
        return null;
    }

    /**
     * Hàm chuyển đổi màn hình SIÊU ĐƠN GIẢN.
     * Chỉ cần truyền Sự kiện click, Đường dẫn FXML của view mới và Tiêu đề cửa sổ.
     *
     * @param event sự kiện kích hoạt chuyển đổi
     * @param fxmlPath đường dẫn tới tệp FXML của giao diện mới
     * @param title tiêu đề của cửa sổ mới
     */
    default void changeView(ActionEvent event, String fxmlPath, String title) {
        Stage stage = getStage(event);
        if (stage == null || fxmlPath == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi nạp màn hình FXML: " + fxmlPath);
        }
    }
}
