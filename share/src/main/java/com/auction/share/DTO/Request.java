package com.auction.share.DTO;

import java.io.Serializable;
import java.util.UUID;

public abstract class Request implements Serializable {
    private String requestId = UUID.randomUUID().toString();
    private final String action;

    protected Request(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
    public Request withUserId(String userId) {
        return this;
    }
    public String getRequestId(){
        return requestId;
    }

}
