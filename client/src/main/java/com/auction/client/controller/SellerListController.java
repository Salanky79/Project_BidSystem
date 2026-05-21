package com.auction.client.controller;

import com.auction.client.ClientContext;
import com.auction.client.service.AuctionService;
import com.auction.client.session.SessionManager;
import com.auction.share.DTO.AuctionSummaryDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

/**
 * Controller dùng chung cho 3 tab: Drafts, Active, Sold.
 * Được gọi từ SellerDashboardFrameController sau khi load SellerListView.fxml.
 */
public class SellerListController {

    @FXML private Label titleLabel;
    @FXML private Label countBadge;
    @FXML private VBox  emptyState;
    @FXML private HBox  loadingBox;
    @FXML private VBox  cardList;
    @FXML private Label emptyIcon;
    @FXML private Label emptyMessage;

    private final AuctionService auctionService = ClientContext.auctionService();

    // =====================================================================
    //  Public API
    // =====================================================================

    /**
     * Load danh sách auction tương ứng với chế độ được chọn.
     *
     * @param mode "Drafts" | "Active" | "Sold"
     */
    public void loadItems(String mode) {
        // Cập nhật tiêu đề
        titleLabel.setText(labelFor(mode));

        // Map mode → AuctionStatus string gửi lên server
        String statusFilter = statusFor(mode);

        // Lấy sellerId từ session
        String sellerId = ClientContext.userService().getSessionManager().getCurrentUserId();

        showLoading(true);
        cardList.getChildren().clear();

        auctionService.getSellerAuctions(sellerId, statusFilter, response ->
            Platform.runLater(() -> {
                showLoading(false);

                if (response == null || !response.isSuccess()) {
                    showEmpty(mode, true);
                    System.out.println("[SellerListController] Failed: " +
                            (response != null ? response.getMessage() : "null response"));
                    return;
                }

                if (!(response.getData() instanceof List<?> list) || list.isEmpty()) {
                    showEmpty(mode, true);
                    return;
                }

                int count = 0;
                for (Object obj : list) {
                    if (obj instanceof AuctionSummaryDTO dto) {
                        appendCard(dto);
                        count++;
                    }
                }

                countBadge.setText(String.valueOf(count));
                showEmpty(mode, count == 0);
            })
        );
    }

    // =====================================================================
    //  Private helpers
    // =====================================================================

    /** Tạo và thêm một SellerItemCard vào danh sách. */
    private void appendCard(AuctionSummaryDTO dto) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/auction/client/view/SellerItemCard.fxml"));
            HBox card = loader.load();

            SellerItemCardController ctrl = loader.getController();
            ctrl.setData(
                    iconForCategory(dto.getCategory()),
                    dto.getCategory(),
                    dto.getItemName(),
                    dto.getCurrentPrice(),
                    0,
                    dto.getEndTime(),
                    dto.getStatus(),
                    dto.getAuctionId()
            );

            cardList.getChildren().add(card);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showLoading(boolean show) {
        loadingBox.setVisible(show);
        loadingBox.setManaged(show);
        if (show) {
            emptyState.setVisible(false);
            emptyState.setManaged(false);
        }
    }

    private void showEmpty(String mode, boolean show) {
        emptyState.setVisible(show);
        emptyState.setManaged(show);
        if (show) {
            countBadge.setText("0");
            emptyIcon.setText(emptyIconFor(mode));
            emptyMessage.setText(emptyMsgFor(mode));
        }
    }

    // =====================================================================
    //  Mapping helpers
    // =====================================================================

    /** Map mode → AuctionStatus string phù hợp với server. */
    private String statusFor(String mode) {
        return switch (mode) {
            case "Drafts" -> "OPEN";
            case "Active" -> "RUNNING";
            case "Sold"   -> "FINISHED";
            default       -> null;  // null = lấy tất cả
        };
    }

    private String labelFor(String mode) {
        return switch (mode) {
            case "Drafts" -> "Drafts  –  Chờ bắt đầu";
            case "Active" -> "Active  –  Đang đấu giá";
            case "Sold"   -> "Sold  –  Đã kết thúc";
            default       -> mode;
        };
    }

    private String emptyIconFor(String mode) {
        return switch (mode) {
            case "Drafts" -> "📝";
            case "Active" -> "🔕";
            case "Sold"   -> "📦";
            default       -> "📭";
        };
    }

    private String emptyMsgFor(String mode) {
        return switch (mode) {
            case "Drafts" -> "Bạn chưa có phiên đấu giá nào đang chờ.";
            case "Active" -> "Không có phiên đấu giá nào đang chạy.";
            case "Sold"   -> "Bạn chưa bán được sản phẩm nào.";
            default       -> "Không có dữ liệu.";
        };
    }

    private String iconForCategory(String category) {
        if (category == null) return "📦";
        return switch (category) {
            case "Electronic"           -> "📱";
            case "Watch", "WATCH"       -> "⌚";
            case "Hand Bag", "Clothing" -> "👜";
            case "Car", "Vehicle"       -> "🚗";
            case "Art"                  -> "🖼";
            case "Jewelry"              -> "💍";
            default                     -> "📦";
        };
    }
}
