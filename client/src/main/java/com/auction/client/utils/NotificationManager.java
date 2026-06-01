package com.auction.client.utils;

import java.util.ArrayList;
import java.util.List;
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

  private static final Duration DISPLAY_DURATION  = Duration.seconds(2.8);
  private static final Duration FADE_IN_DURATION  = Duration.millis(250);
  private static final Duration FADE_OUT_DURATION = Duration.millis(400);

  private static final List<Popup> activePopups = new ArrayList<>();

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

    // Tìm cửa sổ đang hiển thị để attach popup (H6 - ưu tiên focused window)
    Window owner = Window.getWindows().stream()
        .filter(w -> w.isShowing() && w.isFocused())
        .findFirst()
        .orElseGet(() -> Window.getWindows().stream()
            .filter(Window::isShowing)
            .findFirst()
            .orElse(null));

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

    // Tính toán chiều cao dự kiến của toast để dịch chuyển các toast cũ lên trên
    content.applyCss();
    content.layout();
    double toastHeight = content.prefHeight(-1);
    double gap = 10;
    double totalShift = toastHeight + gap;

    // Dịch chuyển tất cả các popup đang hiển thị lên trên để nhường chỗ cho popup mới ở dưới cùng
    for (Popup active : activePopups) {
      active.setY(active.getY() - totalShift);
    }

    // Tính toán vị trí: góc dưới bên phải, offset 20px
    double x = owner.getX() + owner.getWidth() - 320;
    double y = owner.getY() + owner.getHeight() - 120;
    popup.show(owner, x, y);

    // Thêm vào danh sách các popup đang hoạt động
    activePopups.add(popup);

    // Animation: trượt lên + fade in (L4 - dùng hằng số)
    TranslateTransition slide = new TranslateTransition(FADE_IN_DURATION, content);
    slide.setFromY(20);
    slide.setToY(0);

    FadeTransition fadeIn = new FadeTransition(FADE_IN_DURATION, content);
    fadeIn.setFromValue(0.0);
    fadeIn.setToValue(1.0);

    slide.play();
    fadeIn.play();

    // Tự động ẩn sau 3 giây (L4 - dùng hằng số)
    FadeTransition fadeOut = new FadeTransition(FADE_OUT_DURATION, content);
    fadeOut.setDelay(DISPLAY_DURATION);
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);
    fadeOut.setOnFinished(e -> {
      popup.hide();
      activePopups.remove(popup);
    });
    fadeOut.play();
  }
}
