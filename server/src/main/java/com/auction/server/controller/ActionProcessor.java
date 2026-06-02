package com.auction.server.controller;

import com.auction.share.DTO.Request;
import com.auction.share.DTO.Response;

public interface ActionProcessor<T extends Request> {
    Response<?> process(T request) throws Exception;
}
