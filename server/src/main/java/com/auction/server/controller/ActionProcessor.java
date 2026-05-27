package com.auction.server.controller;

import com.auction.server.network.ClientSession;
import com.auction.share.DTO.Request;
import com.auction.share.DTO.Response;

public interface ActionProcessor<T extends Request> {
    Response<?> process(T request, ClientSession session) throws Exception;
}
