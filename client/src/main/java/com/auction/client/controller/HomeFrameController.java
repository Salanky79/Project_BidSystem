package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;

public class HomeFrameController extends FrameController {

    protected HomeController currentHomeController;

    @javafx.fxml.FXML
    private javafx.scene.control.Label budgetLabel;

    private double currentBudget = 0.0;

    @javafx.fxml.FXML
    public void handleTopUp(ActionEvent event) {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("0");
        dialog.setTitle("Nạp tiền");
        dialog.setHeaderText("Nạp tiền vào tài khoản");
        dialog.setContentText("Nhập số tiền muốn nạp:");

        java.util.Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount > 0) {
                    currentBudget += amount;
                    if (budgetLabel != null) {
                        budgetLabel.setText(String.format("%.1f", currentBudget));
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore invalid input
            }
        });
    }

    public void loadHomePage(String filterStatus) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/Home.fxml"));
            Node node = loader.load();

            currentHomeController = loader.getController();
            scrollContent.setContent(node);

            if (currentHomeController != null) {
                currentHomeController.loadAuction(filterStatus);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: Cannot load Home.fxml");
        }
    }

    @Override
    protected void onViewChanged(Node newNode) {
        currentHomeController = null;
    }

    public void handleLogout(ActionEvent event) {
        System.out.println("Logging out... Returning to Login screen.");
        showLogin(event);
    }

    public void handleProfile() {
        changeView("/com/auction/client/view/profile.fxml");
    }

    public void handleActiveListings() {
        loadHomePage("RUNNING");
    }

    public void handleHome() {
        loadHomePage("All");
    }



    public void handleWatchlist() {
        loadHomePage("Watchlist");
    }
}
