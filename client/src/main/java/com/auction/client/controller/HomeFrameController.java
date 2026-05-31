package com.auction.client.controller;

import com.auction.client.ClientContext;
import com.auction.client.network.SocketClient;
import com.auction.client.utils.NotificationManager;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.DTO.ProfileDTO;
import com.auction.share.DTO.Response;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeFrameController extends FrameController {

    protected HomeController currentHomeController;

    @FXML
    private javafx.scene.control.Label budgetLabel;

    private Consumer<Response<?>> bidPushListener;
    private SocketClient.ConnectionStateListener connectionStateListener;

    @FXML
    public void initialize() {
        refreshCurrentBudget();
        bidPushListener = response -> {
            if (response != null && response.isSuccess()) {
                if (response.getData() instanceof BidUpdateEvent
                        && "BID_UPDATED".equals(response.getMessage())) {
                    refreshCurrentBudget();
                    NotificationManager.showInfo("Có giá thầu mới!");
                } else if ("AUCTION_FINISHED".equals(response.getMessage())
                        && response.getData() instanceof String finishedId) {
                    javafx.application.Platform.runLater(() -> {
                        refreshCurrentBudget();
                        if (currentHomeController != null) {
                            currentHomeController.loadAuction(currentHomeController.getCurrentStatusFilter());
                        }
                    });
                }
            }
        };
        ClientContext.socketClient().addPushListener(bidPushListener);

        // D3: Lắng nghe trạng thái kết nối để hiển thị banner
        connectionStateListener = new SocketClient.ConnectionStateListener() {
                @Override
                public void onDisconnected() {
                    if (budgetLabel != null)
                        NotificationManager.showWarning("⚠ Mất kết nối — đang thử lại...");
                }
                @Override
                public void onReconnected() {
                    if (budgetLabel != null)
                        NotificationManager.showSuccess("✔ Đã kết nối lại!");
                    refreshCurrentBudget();
                }
            };
        ClientContext.socketClient().addConnectionStateListener(connectionStateListener);

    }

    public void loadHomePage(String filterStatus) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/Home.fxml"));
            Node node = loader.load();

            currentHomeController = loader.getController();
            scrollContent.setContent(node);

            if (currentHomeController != null) {
                currentHomeController.setAuctionService(ClientContext.auctionService());
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

    public void cleanup() {
        if (bidPushListener != null) {
            ClientContext.socketClient().removePushListener(bidPushListener);
            bidPushListener = null;
        }
        if (connectionStateListener != null) {
            ClientContext.socketClient().removeConnectionStateListener(connectionStateListener);
            connectionStateListener = null;
        }
    }

    public void handleLogout(ActionEvent event) {
        System.out.println("Logging out... Returning to Login screen.");
        cleanup();
        showLogin(event);
    }

    public void handleProfile() {
        changeView("/com/auction/client/view/profile.fxml", obj -> {
            if (obj instanceof com.auction.client.controller.ProfileController profileCtrl) {
                profileCtrl.setUserService(ClientContext.userService());
            }
        });
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
