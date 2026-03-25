module com.example.he_thong_dau_gia_truc_tuyen {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.he_thong_dau_gia_truc_tuyen to javafx.fxml;
    exports com.example.he_thong_dau_gia_truc_tuyen;
}