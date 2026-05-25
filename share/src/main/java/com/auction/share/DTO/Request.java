package com.auction.share.DTO;

import java.io.Serializable;
import java.util.UUID;

/**
 * Lớp cơ sở trừu tượng cho tất cả các yêu cầu (request) gửi lên hệ thống.
 * Cung cấp mã yêu cầu duy nhất và loại hành động.
 */
public abstract class Request implements Serializable {
  private String requestId = UUID.randomUUID().toString();
  private final String action;

  protected Request(String action) {
    this.action = action;
  }

  public String getAction() {
    return action;
  }

  /**
   * Phương thức builder pattern để gắn userId vào request.
   * Cú pháp return this giúp gọi liên tiếp các hàm (method chaining).
   */
  public Request withUserId(String userId) {
    return this;
  }

  public String getRequestId() {
    return requestId;
  }
}
