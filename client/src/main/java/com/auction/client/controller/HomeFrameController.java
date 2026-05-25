package com.auction.client.controller;

import com.auction.client.ClientContext;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.DTO.ProfileDTO;
import com.auction.share.DTO.Response;
import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;

public class HomeFrameController extends FrameController {

    protected HomeController currentHomeController;

    @FXML
    private javafx.scene.control.Label budgetLabel;

    private Consumer<Response<?>> bidPushListener;

    @FXML
    public void initialize() {
        refreshCurrentBudget();
        bidPushListener = response -> {
            if (response != null
                    && response.isSuccess()
                    && response.getData() instanceof BidUpdateEvent
                    && "BID_UPDATED".equals(response.getMessage())) {
                refreshCurrentBudget();
            }
        };
        ClientContext.socketClient().addPushListener(bidPushListener);
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

    private void refreshCurrentBudget() {
        ClientContext.userService().getProfile(response ->
                javafx.application.Platform.runLater(() -> {
                    if (response != null
                            && response.isSuccess()
                            && response.getData() instanceof ProfileDTO profileDTO
                            && profileDTO.getUser() != null
                            && budgetLabel != null) {
                        budgetLabel.setText(String.format("%.1f", profileDTO.getUser().getAvailableBalance()));
                    }
                })
        );
    }
}
