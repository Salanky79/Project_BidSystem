package com.example.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

public class AuctionDetailController {

    // Hàm để đóng popup khi ấn nút "Close" hoặc click ra ngoài
    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handlePostComment(ActionEvent event) {
        System.out.println("Đã gửi bình luận!");
    }
}