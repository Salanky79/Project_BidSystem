package com.auction.server.controller;

import com.auction.server.service.AuctionService;
import com.auction.share.DTO.*;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuctionController {
    private final AuctionService auctionService;

    // quan li chuc nang dau gia
    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    // những thông tin cần cho client ( ket qua thong qua server tra ve)
    public Response<AuctionSummaryDTO> createAuction(CreateAuctionRequest request) throws Exception {
        validateRequiredText(request.getSellerId(), "Seller ID is required.");
        validateRequiredText(request.getItemName(), "Item Name is required.");
        validateRequiredText(request.getStartTime(), "Start Time is required.");
        validateRequiredText(request.getEndTime(), "End Time is required.");

        if (request.getStartingPrice() <= 0) {
            throw new ValidationException("Starting price must be greater than 0.");
        }

        Auction auction = auctionService.createAuction(request);
        
        AuctionSummaryDTO summaryDTO = new AuctionSummaryDTO(
                auction.getId(),
                auction.getItem().getName(),
                auction.getItem().getCategory().name(),
                auction.getCurrentHighestBid(),
                auction.getStatus().name(),
                auction.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        return Response.success("Auction created successfully.", summaryDTO);
    }

    public Response<Boolean> placeBid(PlaceBidRequest request) throws Exception {
        validateRequiredText(request.getAuctionId(), "Auction ID is required.");
        validateRequiredText(request.getBidderId(), "Bidder ID is required.");

        if (request.getAmount() <= 0) {
            throw new ValidationException("Bid amount must be greater than 0.");
        }

        boolean success = auctionService.placeBid(request);
        return Response.success("Bid placed successfully.", success);
    }

    public Response<Boolean> setAutoBid(SetAutoBidRequest request) throws Exception {
        validateRequiredText(request.getAuctionId(), "Auction ID is required.");
        validateRequiredText(request.getBidderId(), "Bidder ID is required.");

        if (request.getMaxBid() <= 0) {
            throw new ValidationException("Auto-bid max must be greater than 0.");
        }
        if (request.getIncrement() <= 0) {
            throw new ValidationException("Auto-bid increment must be greater than 0.");
        }

        boolean success = auctionService.setAutoBid(request);
        return Response.success("Auto-bid configured successfully.", success);
    }

    public Response<Boolean> cancelAutoBid(CancelAutoBidRequest request) throws Exception {
        validateRequiredText(request.getAuctionId(), "Auction ID is required.");
        validateRequiredText(request.getBidderId(), "Bidder ID is required.");

        boolean success = auctionService.cancelAutoBid(request.getAuctionId(), request.getBidderId());
        String message = success ? "Auto-bid canceled successfully." : "No active auto-bid found.";
        return Response.success(message, success);
    }

    public Response<AuctionDetailDTO> getAuctionDetail(GetAuctionDetailRequest request) throws Exception {
        validateRequiredText(request.getAuctionId(), "Auction ID is required.");
        
        AuctionDetailDTO detail = auctionService.getAuctionDetail(request.getAuctionId());
        return Response.success("Auction detail retrieved.", detail);
    }

    public Response<List<AuctionSummaryDTO>> listAuctions(ListAuctionRequest request) throws Exception {
        List<AuctionSummaryDTO> list = auctionService.listAuctions(request);
        return Response.success("Auctions retrieved successfully.", list);
    }

    // HELPER
    private static void validateRequiredText(String value, String message) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
    }
}
