package com.auction.client.utils;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * G1: Hiển thị Toast Notification góc dưới bên phải màn hình.
 *
 * <p>Thay thế các {@code Alert.showAndWait()} cồng kềnh trong môi trường Real-time. Toast tự động
 * mờ dần và biến mất sau 3 giây, không chặn giao diện người dùng.
 *
 * <p>Cách dùng:
 *
 * <pre>{@code
 * NotificationManager.showSuccess("Đặt giá thành công!");
 * NotificationManager.showError("Không thể kết nối máy chủ");
 * NotificationManager.showWarning("Có người vừa trả giá cao hơn bạn!");
 * NotificationManager.showInfo("Phiên đấu giá đang bắt đầu...");
 * }</pre>
 */
public final class NotificationManager {

  private NotificationManager() {}

  public static void showSuccess(String message) {
    show(message, "#2ecc71", "✓");
  }

  public static void showError(String message) {
    show(message, "#e74c3c", "✕");
  }

  public static void showWarning(String message) {
    show(message, "#f39c12", "⚠");
  }

  public static void showInfo(String message) {
    show(message, "#3498db", "ℹ");
  }

  private static void show(String message, String color, String icon) {
    // Đảm bảo luôn chạy trên JavaFX thread
    if (!Platform.isFxApplicationThread()) {
      Platform.runLater(() -> show(message, color, icon));
      return;
    }

    // Tìm cửa sổ đang hiển thị để attach popup
    Window owner = Window.getWindows().stream()
        .filter(Window::isShowing)
        .findFirst()
        .orElse(null);

    if (owner == null) return;

    // Tạo nội dung toast
    Label iconLabel = new Label(icon);
    iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

    Label textLabel = new Label(message);
    textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
    textLabel.setWrapText(true);
    textLabel.setMaxWidth(250);

    VBox content = new VBox(4, iconLabel, textLabel);
    content.setAlignment(Pos.CENTER_LEFT);
    content.setPadding(new Insets(12, 16, 12, 16));
    content.setStyle(
        "-fx-background-color: " + color + "; "
            + "-fx-background-radius: 8px; "
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 12, 0, 0, 4);");
    content.setMinWidth(220);
    content.setMaxWidth(300);

    Popup popup = new Popup();
    popup.getContent().add(content);
    popup.setAutoFix(false);

    // Tính toán vị trí: góc dưới bên phải, offset 20px
    double x = owner.getX() + owner.getWidth() - 320;
    double y = owner.getY() + owner.getHeight() - 120;
    popup.show(owner, x, y);

    // Animation: trượt lên + fade in
    TranslateTransition slide = new TranslateTransition(Duration.millis(250), content);
    slide.setFromY(20);
    slide.setToY(0);

    FadeTransition fadeIn = new FadeTransition(Duration.millis(250), content);
    fadeIn.setFromValue(0.0);
    fadeIn.setToValue(1.0);

    slide.play();
    fadeIn.play();

    // Tự động ẩn sau 3 giây
    FadeTransition fadeOut = new FadeTransition(Duration.millis(400), content);
    fadeOut.setDelay(Duration.seconds(2.8));
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);
    fadeOut.setOnFinished(e -> popup.hide());
    fadeOut.play();
  }
}
