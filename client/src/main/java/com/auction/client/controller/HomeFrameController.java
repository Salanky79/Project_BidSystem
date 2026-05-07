package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;

import java.io.IOException;

public class HomeFrameController {
    private static final String LOGIN_VIEW = "/com/auction/client/view/Login.fxml";
    private static final String SELL_VIEW = "/com/auction/client/view/Sell.fxml";
    private static final String PROFILE_VIEW = "/com/auction/client/view/profile.fxml";
    private static final String HOME_VIEW = "/com/auction/client/view/Home.fxml";

    @FXML
    protected ScrollPane scrollContent;
    protected HomeController currentHomeController;

    public void setView(Node node) {
        scrollContent.setContent(node);
    }

    public void handleLogout(ActionEvent event) {
        System.out.println("Logging out... Returning to Login screen.");
        try {
            SceneNavigator.switchScene(event, LOGIN_VIEW);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: Login.fxml file not found.");
        }
    }

    public void handleSell() {
        changeView(SELL_VIEW);
    }

    public void handleProfile() {
        changeView(PROFILE_VIEW);
    }

    public void handleActiveListings() {
        loadHomePage();
        if (currentHomeController != null) {
            currentHomeController.loadAuction("Active");
        }
    }

    private void changeView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node newNode = loader.load();

            scrollContent.setContent(newNode);
            currentHomeController = null;

            if (newNode instanceof Region) {
                ((Region) newNode).setMaxWidth(Double.MAX_VALUE);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: File not found " + fxmlFile);
        }
    }

    public void handleHome(ActionEvent actionEvent) {
        loadHomePage();
        if (currentHomeController != null) {
            currentHomeController.loadAuction("All");
        }
    }

    public void handleYourListing(ActionEvent actionEvent) {

    }

    public void loadHomePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(HOME_VIEW));
            Node node = loader.load();

            currentHomeController = loader.getController();
            scrollContent.setContent(node);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lá»—i: KhÃ´ng load Ä‘Æ°á»£c Home.fxml");
        }
    }
}

