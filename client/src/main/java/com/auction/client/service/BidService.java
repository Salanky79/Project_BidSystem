package com.auction.client.service;

import com.auction.client.network.SocketClient;
import com.auction.share.DTO.CancelAutoBidRequest;
import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.DTO.RegisterAutoBidRequest;
import com.auction.share.DTO.Response;
import com.auction.share.exceptions.ValidationException;

import java.util.function.Consumer;

/**
 * Dịch vụ xử lý các nghiệp vụ liên quan đến việc đặt cược và tự động trả giá (auto-bid) phía Client.
 * Giao tiếp với Server thông qua SocketClient.
 */
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
        PlaceBidRequest request = new PlaceBidRequest(auctionId, null, amount);
        socketClient.send(request, onResponse);
    }

    public void registerAutoBid(
            String auctionId,
            String maxBidStr,
            String incrementStr,
            double currentPrice,
            Consumer<Response<?>> onResponse
    ) throws ValidationException {
        if (maxBidStr == null || maxBidStr.trim().isEmpty()) {
            throw new ValidationException("Vui long nhap gia toi da.");
        }
        if (incrementStr == null || incrementStr.trim().isEmpty()) {
            throw new ValidationException("Vui long nhap buoc nhay auto-bid.");
        }

        double maxBid;
        double increment;
        try {
            maxBid = Double.parseDouble(maxBidStr);
            increment = Double.parseDouble(incrementStr);
        } catch (NumberFormatException e) {
            throw new ValidationException("Auto-bid khong hop le, vui long nhap so.");
        }

        if (maxBid <= currentPrice) {
            throw new ValidationException("Gia toi da phai cao hon gia hien tai.");
        }
        if (increment <= 0) {
            throw new ValidationException("Buoc nhay auto-bid phai lon hon 0.");
        }

        RegisterAutoBidRequest request = new RegisterAutoBidRequest(auctionId, maxBid, increment, null);
        socketClient.send(request, onResponse);
    }

    public void setAutoBid(
            String auctionId,
            String maxBidStr,
            String incrementStr,
            double currentPrice,
            Consumer<Response<?>> onResponse
    ) throws ValidationException {
        registerAutoBid(auctionId, maxBidStr, incrementStr, currentPrice, onResponse);
    }

    public void cancelAutoBid(String auctionId, Consumer<Response<?>> onResponse) throws ValidationException {
        if (auctionId == null || auctionId.isBlank()) {
            throw new ValidationException("Auction ID is required.");
        }

        CancelAutoBidRequest request = new CancelAutoBidRequest(auctionId, null);
        socketClient.send(request, onResponse);
    }
}
