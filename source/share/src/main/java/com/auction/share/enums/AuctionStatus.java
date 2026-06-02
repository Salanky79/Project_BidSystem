package com.auction.share.enums;

/**
 * Trạng thái vòng đời của phiên đấu giá.
 *
 * <p>Enum này là nguồn sự thật duy nhất (Single Source of Truth) cho: - Display name thân thiện
 * với người dùng - CSS style badge tương ứng - Logic lọc (filter) trên UI
 *
 * <p>Toàn bộ client code nên dùng {@link #from(String)} để parse string từ server, tránh magic
 * string rải rác.
 */
public enum AuctionStatus {
  OPEN("Open", "#4C8CE4"),
  RUNNING("Active", "#2ecc71"),
  FINISHED("End", "#FF3737"),
  CANCELED("Canceled", "#605B51");

  private final String displayName;
  private final String badgeColor;

  AuctionStatus(String displayName, String badgeColor) {
    this.displayName = displayName;
    this.badgeColor = badgeColor;
  }

  /**
   * Parse từ bất kỳ string nào server trả về — bao gồm cả display name và raw enum name.
   * Không bao giờ trả về null.
   */
  public static AuctionStatus from(String raw) {
    if (raw == null) return CANCELED;
    return switch (raw.trim().toUpperCase()) {
      case "RUNNING", "ACTIVE" -> RUNNING;
      case "OPEN", "IN QUEUE", "IN_QUEUE" -> OPEN;
      case "FINISHED", "END" -> FINISHED;
      default -> CANCELED;
    };
  }

  /** Tên hiển thị thân thiện với người dùng (dùng trong Label, Card). */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * CSS style đầy đủ cho status badge (dùng trong {@code statusLabel.setStyle(...)}).
   */
  public String getBadgeStyle() {
    return "-fx-background-color: " + badgeColor + "; "
        + "-fx-text-fill: white; "
        + "-fx-font-weight: bold; "
        + "-fx-background-radius: 5px; "
        + "-fx-padding: 2 5 2 5;";
  }

  /** Trả về true nếu status này nên hiện trên UI (ẩn CANCELED khỏi danh sách mặc định). */
  public boolean isVisible() {
    return this != CANCELED;
  }

  /** Trả về true nếu phiên đấu giá đang hoạt động (đang chạy hoặc trong hàng chờ). */
  public boolean isActive() {
    return this == RUNNING || this == OPEN;
  }
}
