package com.auction.client.service;

import com.auction.client.network.SocketClient;
import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.DTO.Response;
import com.auction.share.exceptions.ValidationException;

import java.util.function.Consumer;

public class BidService {
    private final SocketClient socketClient;

    public BidService(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    public void placeBid(String auctionId, String amountStr, double currentPrice, Consumer<Response<?>> onResponse) throws ValidationException {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new ValidationException("Vui lòng nhập giá bid.");
        }
        
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            throw new ValidationException("Giá bid không hợp lệ, vui lòng nhập số.");
        }

        if (amount <= currentPrice) {
            throw new ValidationException("Giá bid phải cao hơn giá hiện tại: $" + String.format("%.2f", currentPrice));
        }
        PlaceBidRequest request = new PlaceBidRequest(null, auctionId, amount);
        socketClient.send(request, onResponse);
    }
}
