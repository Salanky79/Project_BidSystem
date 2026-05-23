package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;

public class HomeFrameController extends FrameController {

    protected HomeController currentHomeController;

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

    public void handleYourListing() {
    }

    public void handleWatchlist() {
        loadHomePage("Watchlist");
    }
}
