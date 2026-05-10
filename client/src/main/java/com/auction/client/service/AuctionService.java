package com.auction.client.service;

import com.auction.client.network.AuctionNetwork;
import com.auction.share.DTO.Response;
import com.auction.share.exceptions.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

public class AuctionService {
    private final AuctionNetwork auctionNetwork;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AuctionService(AuctionNetwork auctionNetwork) {
        this.auctionNetwork = auctionNetwork;
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

        auctionNetwork.createAuction(itemName, description, category, startingPrice, startTimeStr, endTimeStr, onResponse);
    }

    public void getAuctions(Consumer<Response<?>> onResponse) {
        auctionNetwork.getAuctions(onResponse);
    }
}
