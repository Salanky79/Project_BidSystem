package com.auction.client.network;

import com.auction.share.DTO.CreateAuctionRequest;
import com.auction.share.DTO.ListAuctionRequest;
import com.auction.share.DTO.Request;
import com.auction.share.DTO.Response;
import java.util.function.Consumer;

public class AuctionNetwork {
    private final SocketClient socketClient;

    public AuctionNetwork(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    public void createAuction(String itemName, String description, String category, double startingPrice, String startTime, String endTime, Consumer<Response<?>> onResponse) {
        CreateAuctionRequest request = new CreateAuctionRequest(null, itemName, description, category, startingPrice, startTime, endTime);
        socketClient.send(request, onResponse);
    }
    
    public void getAuctions(Consumer<Response<?>> onResponse) {
        socketClient.send(new ListAuctionRequest(), onResponse);
    }
}
