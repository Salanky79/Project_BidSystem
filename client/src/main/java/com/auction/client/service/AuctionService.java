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
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public AuctionService(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    public void createAuction(String itemName, String description, String category, String startingPriceStr, String startTimeStr, String endTimeStr, boolean isDraft, Consumer<Response<?>> onResponse) throws ValidationException {
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
            throw new ValidationException("Định dạng thời gian không hợp lệ!");
        }

        CreateAuctionRequest request = new CreateAuctionRequest(null, itemName, description, category, startingPrice, startTimeStr, endTimeStr, isDraft);
        socketClient.send(request, onResponse);
    }

    public void getAuctions(Consumer<Response<?>> onResponse) {
        socketClient.send(new ListAuctionRequest(), onResponse);
    }

    /**
     * Lấy danh sách auction của một seller cụ thể, có thể lọc thêm theo status.
     * @param sellerId  ID của seller hiện tại
     * @param status    Trạng thái auction: "OPEN", "RUNNING", "FINISHED" hoặc null để lấy tất cả
     * @param onResponse callback nhận kết quả từ server
     */
    public void getSellerAuctions(String sellerId, String status, Consumer<Response<?>> onResponse) {
        socketClient.send(new ListAuctionRequest(status, sellerId), onResponse);
    }

    public void cancelAuction(String auctionId, Consumer<Response<?>> onResponse) {
        com.auction.share.DTO.CancelAuctionRequest request = new com.auction.share.DTO.CancelAuctionRequest(auctionId);
        socketClient.send(request, onResponse);
    }

    public void setBidStep(String auctionId, double bidStep, Consumer<Response<?>> onResponse) {
        com.auction.share.DTO.SetBidStepRequest request = new com.auction.share.DTO.SetBidStepRequest(auctionId, bidStep, null);
        socketClient.send(request, onResponse);
    }

    public void extendEndTime(String auctionId, long minutes, Consumer<Response<?>> onResponse) {
        com.auction.share.DTO.ExtendEndTimeRequest request = new com.auction.share.DTO.ExtendEndTimeRequest(auctionId, minutes, null);
        socketClient.send(request, onResponse);
    }

    public void getAuctionDetail(String auctionId, Consumer<Response<?>> onResponse) {
        com.auction.share.DTO.GetAuctionDetailRequest request = new com.auction.share.DTO.GetAuctionDetailRequest(auctionId);
        socketClient.send(request, onResponse);
    }
}
