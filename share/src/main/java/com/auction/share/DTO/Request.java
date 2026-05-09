package com.auction.share.DTO;

import java.io.Serializable;

public abstract class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String action;

    protected Request(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}
