package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class SellerDashboardController extends FrameController {

    @FXML
    private Label lblTotalRevenue;
    @FXML
    private Label lblActiveAuctions;
    @FXML
    private Label lblCompletedSales;
    @FXML
    private Label lblAwaitingShipment;

    @FXML
    private Button btnCreateNewAuction;
    @FXML
    private Button btnLogOut;

    @FXML
    public void initialize() {
        System.out.println("Auction Dashboard Controller initialized!");
    }

    @FXML
    void handleCreateNewAuction(ActionEvent event) {
        changeView("/com/auction/client/view/Sell.fxml");
    }

    @FXML
    void handleLogOut(ActionEvent event) {
        handleLogout(event);
    }

    @FXML
    void handleViewBids(ActionEvent event) {
        Button clickedBtn = (Button) event.getSource();
        System.out.println("View bids: " + clickedBtn.getText());
    }

    @FXML
    void handleEditAuction(ActionEvent event) {
        System.out.println("Edit auction.");
    }

    @FXML
    void handleManageSystem(ActionEvent event) {
        System.out.println("Open system management.");
    }

    @FXML
    void handleCreateInvoice(ActionEvent event) {
        System.out.println("Create invoice.");
    }

    @FXML
    void handleMenuNavigation(ActionEvent event) {
        Button btn = (Button) event.getSource();
        System.out.println("Navigate to menu: " + btn.getText().trim());
    }

    public void handleLogout(ActionEvent actionEvent) {
        showLogin(actionEvent);
    }

    public void handleHome(ActionEvent actionEvent) {
        changeView("/com/auction/client/view/SellerDashboard.fxml");
    }

    public void handleDrafts(ActionEvent actionEvent) {
        changeView("/com/auction/client/view/SellerDashboard.fxml");
    }

    public void handleActive(ActionEvent actionEvent) {
        changeView("/com/auction/client/view/SellerDashboard.fxml");
    }

    public void handleSold(ActionEvent actionEvent) {
        changeView("/com/auction/client/view/SellerDashboard.fxml");
    }

    public void handleProfile(ActionEvent actionEvent) {
        changeView("/com/auction/client/view/profile.fxml");
    }

    public void handleProfile() {
        changeView("/com/auction/client/view/profile.fxml");
    }
}
