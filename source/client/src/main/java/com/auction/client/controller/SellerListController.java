package com.auction.client.controller;

import com.auction.client.service.AuctionService;
import com.auction.client.utils.CategoryUtils;
import com.auction.share.DTO.AuctionSummaryDTO;
import java.io.IOException;
import java.util.List;

import com.auction.share.enums.AuctionStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SellerListController {

  @FXML private Label titleLabel;
  @FXML private Label countBadge;
  @FXML private VBox emptyState;
  @FXML private HBox loadingBox;
  @FXML private VBox cardList;
  @FXML private Label emptyIcon;
  @FXML private Label emptyMessage;

  private AuctionService auctionService;

  public void setAuctionService(AuctionService auctionService) {
    this.auctionService = auctionService;
  }

  public void loadItems(String mode) {
    titleLabel.setText(labelFor(mode));

    showLoading(true);
    cardList.getChildren().clear();

    if (auctionService == null) return;
    auctionService.getSellerAuctions(
        null,
        response ->
            Platform.runLater(
                () -> {
                  showLoading(false);

                  if (response == null || !response.isSuccess()) {
                    showEmpty(mode, true);
                    System.out.println(
                        "[SellerListController] Failed: "
                            + (response != null ? response.getMessage() : "null response"));
                    return;
                  }

                  if (!(response.getData() instanceof List<?> list) || list.isEmpty()) {
                    showEmpty(mode, true);
                    return;
                  }

                  int count = 0;
                  for (Object obj : list) {
                    if (obj instanceof AuctionSummaryDTO dto) {
                      if (matchesMode(mode, dto)) {
                        appendCard(dto, () -> loadItems(mode));
                        count++;
                      }
                    }
                  }

                  countBadge.setText(String.valueOf(count));
                  showEmpty(mode, count == 0);
                }));
  }

  private boolean matchesMode(String mode, AuctionSummaryDTO dto) {
    AuctionStatus status = AuctionStatus.from(dto.getStatus());
    if (status == AuctionStatus.CANCELED && !"Sold".equals(mode)) {
      return false;
    }

    return switch (mode) {
      case "Active" -> status.isActive();
      case "Sold" -> status == AuctionStatus.FINISHED || status == AuctionStatus.CANCELED;
      default -> true;
    };
  }

  private void appendCard(AuctionSummaryDTO dto, Runnable refreshCallback) {
    try {
      FXMLLoader loader =
          new FXMLLoader(getClass().getResource("/com/auction/client/view/SellerItemCard.fxml"));
      HBox card = loader.load();

      SellerItemCardController ctrl = loader.getController();
      ctrl.setRefreshCallback(refreshCallback);
      ctrl.setData(
          CategoryUtils.iconFor(dto.getCategory()),
          dto.getCategory(),
          dto.getItemName(),
          dto.getCurrentPrice(),
          dto.getBidCount(),
          dto.getEndTime(),
          dto.getStatus(),
          dto.getAuctionId(),
          dto.getImageUrl());

      cardList.getChildren().add(card);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void showLoading(boolean show) {
    if (loadingBox != null) {
      loadingBox.setVisible(show);
      loadingBox.setManaged(show);
    }
    if (emptyState != null && show) {
      emptyState.setVisible(false);
      emptyState.setManaged(false);
    }
  }

  private void showEmpty(String mode, boolean show) {
    if (emptyState != null) {
      emptyState.setVisible(show);
      emptyState.setManaged(show);
    }
    if (show) {
      if (countBadge != null) countBadge.setText("0");
      if (emptyIcon != null) emptyIcon.setText(emptyIconFor(mode));
      if (emptyMessage != null) emptyMessage.setText(emptyMsgFor(mode));
    }
  }

  private String labelFor(String mode) {
    return switch (mode) {
      case "Active" -> "Active - Ongoing";
      case "Sold" -> "Sold - Finished";
      default -> mode;
    };
  }

  private String emptyIconFor(String mode) {
    return switch (mode) {
      case "Active" -> "🔕";
      case "Sold" -> "📦";
      default -> "📭";
    };
  }

  private String emptyMsgFor(String mode) {
    return switch (mode) {
      case "Active" -> "No active auctions currently.";
      case "Sold" -> "You haven't sold any items yet.";
      default -> "No data available.";
    };
  }


}
