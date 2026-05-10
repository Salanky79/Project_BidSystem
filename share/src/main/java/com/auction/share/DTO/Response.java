package com.auction.share.DTO;

import java.io.Serializable;

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

    public boolean isSuccess() {
        return success;
    }

    public void setResquestId(String resquestId){
        this.resquestId = resquestId;
    }
    public String getRequestId(){
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
