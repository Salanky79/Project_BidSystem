package com.auction.client.service;

import com.auction.client.network.SocketClient;
import com.auction.share.DTO.CreateAuctionRequest;
import com.auction.share.DTO.ListAuctionRequest;
import com.auction.share.DTO.Response;
import com.auction.share.exceptions.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

public class AuctionService {
    private final SocketClient socketClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AuctionService(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    public void createAuction(String itemName, String description, String category, String startingPriceStr, String startTimeStr, String endTimeStr, Consumer<Response<?>> onResponse) throws ValidationException {
        if (itemName == null || itemName.trim().isEmpty()) {
            throw new ValidationException("Tên sản phẩm không được để trống!");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Danh mục không được để trống!");
        }
        
        double startingPrice;
        try {
            startingPrice = Double.parseDouble(startingPriceStr);
            if (startingPrice <= 0) {
                throw new ValidationException("Giá khởi điểm phải lớn hơn 0!");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("Giá khởi điểm không hợp lệ!");
        }

        try {
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, FORMATTER);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, FORMATTER);
            if (endTime.isBefore(startTime)) {
                throw new ValidationException("Thời gian kết thúc phải sau thời gian bắt đầu!");
            }
        } catch (DateTimeParseException e) {
            throw new ValidationException("Định dạng thời gian không hợp lệ (yêu cầu: dd/MM/yyyy HH:mm)!");
        }

        CreateAuctionRequest request = new CreateAuctionRequest(null, itemName, description, category, startingPrice, startTimeStr, endTimeStr);
        socketClient.send(request, onResponse);
    }

    public void getAuctions(Consumer<Response<?>> onResponse) {
        socketClient.send(new ListAuctionRequest(), onResponse);
    }
}
