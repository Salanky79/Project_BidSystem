package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class SellerDashboardFrameController extends FrameController {

    @FXML private Label  usernameLabel;
    @FXML private ScrollPane scrollContent;

    // Sidebar buttons – cần fx:id để highlight
    @FXML private Button btnHome;
    @FXML private Button btnActive;
    @FXML private Button btnSold;
    @FXML private Button btnProfile;
    @FXML private Button btnSell;

    // ── Styles ──────────────────────────────────────────────────────────────
    private static final String STYLE_SIDEBAR_ACTIVE =
            "-fx-background-color: #f0a500; -fx-text-fill: white; " +
            "-fx-font-size: 15px; -fx-font-weight: bold; " +
            "-fx-border-radius: 8; -fx-background-radius: 8; " +
            "-fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 20; -fx-cursor: hand;";

    private static final String STYLE_SIDEBAR_INACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: white; " +
            "-fx-font-size: 15px; " +
            "-fx-border-radius: 8; -fx-background-radius: 8; " +
            "-fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 20; -fx-cursor: hand;";

    private static final String STYLE_SIDEBAR_INACTIVE_INDENT =
            "-fx-background-color: transparent; -fx-text-fill: #c9c9c9; " +
            "-fx-font-size: 15px; " +
            "-fx-border-radius: 8; -fx-background-radius: 8; " +
            "-fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 40; -fx-cursor: hand;";

    private static final String STYLE_SIDEBAR_ACTIVE_INDENT =
            "-fx-background-color: #f0a500; -fx-text-fill: white; " +
            "-fx-font-size: 15px; -fx-font-weight: bold; " +
            "-fx-border-radius: 8; -fx-background-radius: 8; " +
            "-fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 40; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        loadView("/com/auction/client/view/SellerDashboard.fxml");
        highlightButton("Home");
    }

    // ===== TOPBAR =====

    @FXML
    public void handleLogout(ActionEvent event) {
        showLogin(event);
    }

    // ===== SIDEBAR NAVIGATION =====

    @FXML
    public void handleHome(ActionEvent event) {
        loadView("/com/auction/client/view/SellerDashboard.fxml");
        highlightButton("Home");
    }

    @FXML
    public void handleActive(ActionEvent event) {
        loadSellerList("Active");
        highlightButton("Active");
    }

    @FXML
    public void handleSold(ActionEvent event) {
        loadSellerList("Sold");
        highlightButton("Sold");
    }

    @FXML
    public void handleProfile(ActionEvent event) {
        changeView("/com/auction/client/view/profile.fxml");
        highlightButton("Profile");
    }

    @FXML
    public void handleSell(ActionEvent event) {
        changeView("/com/auction/client/view/Sell.fxml");
    }

    // ===== HELPERS =====

    /**
     * Load SellerListView.fxml vào scrollContent rồi gọi loadItems(mode)
     * để fetch đúng tab (Active / Sold).
     */
    private void loadSellerList(String mode) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/auction/client/view/SellerListView.fxml"));
            VBox view = loader.load();
            scrollContent.setContent(view);

            SellerListController ctrl = loader.getController();
            ctrl.loadItems(mode);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[SellerDashboardFrame] Cannot load SellerListView.fxml");
        }
    }

    /**
     * Load một FXML đơn giản (không cần gọi thêm method) vào scrollContent.
     */
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            VBox view = loader.load();
            scrollContent.setContent(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Highlight đúng button trên sidebar tương ứng với màn hình đang hiển thị.
     */
    private void highlightButton(String active) {
        // Top-level buttons (không indent)
        if (btnHome    != null) btnHome.setStyle(   "Home".equals(active)    ? STYLE_SIDEBAR_ACTIVE    : STYLE_SIDEBAR_INACTIVE);
        if (btnProfile != null) btnProfile.setStyle("Profile".equals(active) ? STYLE_SIDEBAR_ACTIVE    : STYLE_SIDEBAR_INACTIVE);

        // Indented sub-buttons
        if (btnActive  != null) btnActive.setStyle( "Active".equals(active)  ? STYLE_SIDEBAR_ACTIVE_INDENT : STYLE_SIDEBAR_INACTIVE_INDENT);
        if (btnSold    != null) btnSold.setStyle(   "Sold".equals(active)    ? STYLE_SIDEBAR_ACTIVE_INDENT : STYLE_SIDEBAR_INACTIVE_INDENT);
    }
}