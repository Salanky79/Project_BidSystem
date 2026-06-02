package com.auction.share.DTO;

import java.io.Serializable;

/**
 * Lớp bọc (wrapper) chung cho các phản hồi (response) trả về từ hệ thống.
 * Cú pháp {@code <T>} (Generic) cho phép lớp này chứa dữ liệu thuộc nhiều kiểu khác nhau.
 */
public class Response<T> implements Serializable {
  private String resquestId;

  private final boolean success;
  private final String message;
  private final T data;

  private String authenticatedUserId;

  public Response(boolean success, String message, T data) {
    this.success = success;
    this.message = message;
    this.data = data;
  }

  public String getAuthenticatedUserId() {
      return authenticatedUserId;
  }

  public void setAuthenticatedUserId(String authenticatedUserId) {
      this.authenticatedUserId = authenticatedUserId;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setRequestId(String resquestId) {
    this.resquestId = resquestId;
  }

  public String getRequestId() {
    return resquestId;
  }

  public String getMessage() {
    return message;
  }

  public T getData() {
    return data;
  }

  public static <T> Response<T> success(String message, T data) {
    return new Response<>(true, message, data);
  }

  public static <T> Response<T> fail(String message) {
    return new Response<>(false, message, null);
  }
}
