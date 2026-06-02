package com.auction.share.DTO;

import java.io.Serializable;
import java.util.UUID;

/**
 * Lớp cơ sở trừu tượng cho tất cả các yêu cầu (request) gửi lên hệ thống.
 * Cung cấp mã yêu cầu duy nhất và loại hành động.
 */
public abstract class Request implements Serializable {
  private String requestId = UUID.randomUUID().toString();
  private String userId;

  /** Cho phép copy requestId gốc khi request được truyền qua các lớp xử lý khác nhau. */
  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }
  private final String action;

  protected Request(String action) {
    this.action = action;
  }

  public String getAction() {
    return action;
  }

  public String getUserId() {
    return userId;
  }

  /** Gắn userId vào request hiện tại và trả về chính nó để dùng chaining nếu cần. */
  public Request withUserId(String userId) {
    this.userId = userId;
    return this;
  }

  public String getRequestId() {
    return requestId;
  }
}
