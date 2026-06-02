package com.auction.client.utils;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * Utility class để load ảnh từ URL vào card UI.
 *
 * <p>Thay thế code load ảnh trùng lặp giữa {@code ItemCardController} và
 * {@code SellerItemCardController}. Xử lý: background loading, clip bo tròn góc,
 * fallback về emoji icon khi URL null hoặc lỗi.
 */
public final class CardImageLoader {

  private CardImageLoader() {}

  /**
   * Load ảnh từ URL vào StackPane card. Fallback về emoji icon nếu URL null hoặc lỗi 4xx/5xx.
   *
   * @param container     StackPane chứa ảnh (itemcard)
   * @param fallbackLabel Label hiển thị icon emoji khi không có ảnh
   * @param imageUrl      URL ảnh từ Cloudinary (có thể null)
   * @param fallbackIcon  Emoji icon fallback
   * @param size          Kích thước ảnh (width = height)
   * @param arcSize       Bo tròn góc (arcWidth = arcHeight)
   */
  public static void load(
      StackPane container,
      Label fallbackLabel,
      String imageUrl,
      String fallbackIcon,
      double size,
      double arcSize) {

    if (imageUrl != null && !imageUrl.isBlank()) {
      fallbackLabel.setVisible(false);

      ImageView imageView = new ImageView();
      imageView.setFitWidth(size);
      imageView.setFitHeight(size);
      imageView.setPreserveRatio(false);

      // backgroundLoading = true: load bất đồng bộ, không block JavaFX thread
      Image image = new Image(imageUrl, size, size, false, true, true);
      imageView.setImage(image);

      // Fallback về emoji icon nếu URL lỗi (404, timeout, v.v.)
      image.errorProperty().addListener((obs, wasError, isError) -> {
        if (isError) {
          Platform.runLater(() -> {
            fallbackLabel.setVisible(true);
            fallbackLabel.setText(fallbackIcon);
            if (container != null) {
              container.getChildren().removeIf(n -> n instanceof ImageView);
            }
          });
        }
      });

      // Clip bo tròn góc khớp với CSS border-radius của StackPane
      Rectangle clip = new Rectangle(size, size);
      clip.setArcWidth(arcSize);
      clip.setArcHeight(arcSize);
      imageView.setClip(clip);

      if (container != null) {
        container.getChildren().removeIf(n -> n instanceof ImageView);
        container.getChildren().add(0, imageView);
      }
    } else {
      // Không có ảnh — hiển thị emoji icon
      fallbackLabel.setVisible(true);
      fallbackLabel.setText(fallbackIcon);
      if (container != null) {
        container.getChildren().removeIf(n -> n instanceof ImageView);
      }
    }
  }
}
