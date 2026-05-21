package com.auction.share.DTO;

import java.io.Serializable;

// Generic <T> giúp dùng chung Response cho nhiều kiểu dữ liệu.
/**
 * Kết quả xử lý trả về từ Server.
 */
public class Response<T> implements Serializable {
    private String resquestId;
    private final boolean success;
    private final String message;
    private final T data;

    public Response(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * Tạo response thành công.
     */
    public static <T> Response<T> success(String message, T data) {
        return new Response<>(true, message, data);
    }

    /**
     * Tạo response thất bại.
     */
    public static <T> Response<T> fail(String message) {
        return new Response<>(false, message, null);
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
}