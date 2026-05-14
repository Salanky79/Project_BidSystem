package com.auction.client.network;

import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.DTO.Response;
import java.util.function.Consumer;

public class BidNetwork {
    private final SocketClient socketClient;

    public BidNetwork(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    public void placeBid(String auctionId, double amount, Consumer<Response<?>> onResponse) {
        PlaceBidRequest request = new PlaceBidRequest(auctionId, null, amount);
        socketClient.send(request, onResponse);
    }
}
