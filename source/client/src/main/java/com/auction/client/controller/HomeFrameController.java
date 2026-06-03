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
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeFrameController extends FrameController {

    protected HomeController currentHomeController;
    protected ProfileController currentProfileController;

    @FXML
    private javafx.scene.control.Label budgetLabel;

    private Consumer<Response<?>> bidPushListener;


    @FXML
    public void initialize() {
        refreshCurrentBudget(null);
        bidPushListener = response -> {
            if (response != null && response.isSuccess()) {
                if (response.getData() instanceof BidUpdateEvent event
                        && "BID_UPDATED".equals(response.getMessage())) {
                    refreshCurrentBudget(event);
                    if (currentHomeController != null) {
                        currentHomeController.updateCardOnBidEvent(event);
                    }
                } else if (("AUCTION_FINISHED".equals(response.getMessage()) || "AUCTION_CANCELLED".equals(response.getMessage()))
                        && response.getData() instanceof String auctionId) {
                    javafx.application.Platform.runLater(() -> {
                        refreshCurrentBudget(null);
                        if (currentHomeController != null) {
                            currentHomeController.loadAuction(currentHomeController.getCurrentStatusFilter());
                        }
                    });
                }
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
        currentProfileController = null;
    }

    public void cleanup() {
        if (bidPushListener != null) {
            ClientContext.socketClient().removePushListener(bidPushListener);
            bidPushListener = null;
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
                currentProfileController = profileCtrl;
            }
        });
    }

    public void handleActiveListings() {
        loadHomePage("RUNNING");
    }

    public void handleHome() {
        loadHomePage("All");
    }

    private void refreshCurrentBudget(BidUpdateEvent event) {
        ClientContext.userService().getProfile(response ->
                javafx.application.Platform.runLater(() -> {
                    if (response != null
                            && response.isSuccess()
                            && response.getData() instanceof ProfileDTO profileDTO
                            && profileDTO.getUser() != null) {
                        
                        if (budgetLabel != null) {
                            budgetLabel.setText(String.format("%.1f", profileDTO.getUser().getAvailableBalance()));
                        }

                        if (currentProfileController != null) {
                            currentProfileController.loadProfileData();
                        }

                        if (event != null) {
                            if (profileDTO.getUser().getId().equals(event.getBidderId())) {
                                // NotificationManager.showInfo("Bạn đã đặt giá thành công!"); // Có thể bỏ qua vì UI đã báo
                            } else {
                                NotificationManager.showInfo("Có giá thầu mới!");
                            }
                        }
                    }
                })
        );
    }

    @FXML
    public void handleDeposit(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("1000");
        dialog.setTitle("Nạp tiền");
        dialog.setHeaderText("Nạp tiền vào tài khoản");
        dialog.setContentText("Nhập số tiền (VND):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    NotificationManager.showError("Số tiền nạp phải lớn hơn 0.");
                    return;
                }
                ClientContext.userService().deposit(amount, response -> {
                    Platform.runLater(() -> {
                        if (response != null && response.isSuccess()) {
                            NotificationManager.showSuccess("Nạp tiền thành công!");
                            refreshCurrentBudget(null);
                        } else {
                            NotificationManager.showError("Nạp tiền thất bại: " + (response != null ? response.getMessage() : "Unknown"));
                        }
                    });
                });
            } catch (NumberFormatException e) {
                NotificationManager.showError("Số tiền không hợp lệ.");
            } catch (Exception e) {
                NotificationManager.showError("Lỗi hệ thống.");
            }
        });
    }
}
