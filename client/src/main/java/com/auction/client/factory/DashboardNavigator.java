package com.auction.client.factory;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Lớp tiện ích (Utility class) để điều hướng (navigate) giữa các màn hình Dashboard.
 */
public final class DashboardNavigator {

    private DashboardNavigator() {
    }

    public static void openDashboard(Stage stage, String role) throws IOException {
        DashboardProduct dashboard = createDashboard(role);
        Scene scene = dashboard.getScene();

        stage.setScene(scene);
        stage.setTitle(dashboard.getTitle());
        stage.centerOnScreen();
        stage.show();
    }

    // áp dụng Factory Method pattern: tạo dashboard tương ứng dựa trên role (vai trò)
    private static DashboardProduct createDashboard(String role) {
        RoleUIFactory factory;
        if ("Seller".equalsIgnoreCase(role)) {
            factory = new SellerUIFactory();
        } else {
            factory = new BidderUIFactory();
        }
        return factory.createDashboard();
    }
}
