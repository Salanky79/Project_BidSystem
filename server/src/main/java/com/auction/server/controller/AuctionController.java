package com.auction.server.controller;

import com.auction.server.service.AuctionService;
import com.auction.server.service.AutoBidService;
import com.auction.share.DTO.*;
import com.auction.server.mapper.AuctionMapper;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;

import java.util.List;

/**
 * Controller xử lý các yêu cầu liên quan đến phiên đấu giá (tạo mới, hủy, đặt giá, tự động đấu
 * giá).
 */
public class AuctionController {
  private final AuctionService auctionService;
  private final AutoBidService autoBidService;
  private final com.auction.server.service.BidService bidService;
  private final com.auction.server.service.AuctionQueryService auctionQueryService;
  private final com.auction.server.service.BroadcastService broadcastService;

  // khởi tạo controller với các service tương ứng
  public AuctionController(
      AuctionService auctionService,
      AutoBidService autoBidService,
      com.auction.server.service.BidService bidService,
      com.auction.server.service.AuctionQueryService auctionQueryService,
      com.auction.server.service.BroadcastService broadcastService) {
    this.auctionService = auctionService;
    this.autoBidService = autoBidService;
    this.bidService = bidService;
    this.auctionQueryService = auctionQueryService;
    this.broadcastService = broadcastService;
  }

  // xử lý logic tạo phiên đấu giá và chuẩn bị DTO trả về cho client
  public Response<AuctionSummaryDTO> createAuction(CreateAuctionRequest request) throws Exception {
    validateRequiredText(request.getUserId(), "Seller ID is required.");
    validateRequiredText(request.getItemName(), "Item Name is required.");
    validateRequiredText(request.getStartTime(), "Start Time is required.");
    validateRequiredText(request.getEndTime(), "End Time is required.");

    if (request.getStartingPrice() <= 0) {
      throw new ValidationException("Starting price must be greater than 0.");
    }

    Auction auction = auctionService.createAuction(request);

    AuctionSummaryDTO summaryDTO = AuctionMapper.toSummaryDTO(auction);

    return Response.success("Auction created successfully.", summaryDTO);
  }

  public Response<Boolean> cancelAuction(CancelAuctionRequest request)
      throws Exception {
    validateRequiredText(request.getAuctionId(), "Auction ID is required.");
    validateRequiredText(request.getUserId(), "Authentication required.");
    auctionService.cancelAuction(request.getAuctionId(), request.getUserId());
    broadcastService.broadcastAuctionCancelled(request.getAuctionId());
    return Response.success("Auction canceled successfully.", true);
  }

  public Response<Boolean> placeBid(PlaceBidRequest request) throws Exception {
    validateRequiredText(request.getAuctionId(), "Auction ID is required.");
    validateRequiredText(request.getUserId(), "Bidder ID is required.");

    if (request.getAmount() <= 0) {
      throw new ValidationException("Bid amount must be greater than 0.");
    }

    boolean success = bidService.placeBid(request, true);
    return Response.success("Bid placed successfully.", success);
  }

  public Response<Boolean> registerAutoBid(RegisterAutoBidRequest request) throws Exception {
    validateRequiredText(request.getAuctionId(), "Auction ID is required.");
    validateRequiredText(request.getUserId(), "Bidder ID is required.");

    if (request.getMaxBid() <= 0) {
      throw new ValidationException("Auto-bid max must be greater than 0.");
    }
    if (request.getIncrement() <= 0) {
      throw new ValidationException("Auto-bid increment must be greater than 0.");
    }

    boolean success = autoBidService.register(request);
    return Response.success("Auto-bid configured successfully.", success);
  }

  public Response<Boolean> cancelAutoBid(CancelAutoBidRequest request) throws Exception {
    validateRequiredText(request.getAuctionId(), "Auction ID is required.");
    validateRequiredText(request.getUserId(), "Bidder ID is required.");

    boolean success = autoBidService.cancel(request);
    String message = success ? "Auto-bid canceled successfully." : "No active auto-bid found.";
    return Response.success(message, success);
  }

  public Response<AuctionDetailDTO> getAuctionDetail(GetAuctionDetailRequest request)
      throws Exception {
    validateRequiredText(request.getAuctionId(), "Auction ID is required.");

    AuctionDetailDTO detail = auctionQueryService.getAuctionDetail(request.getAuctionId());
    return Response.success("Auction detail retrieved.", detail);
  }

  public Response<List<AuctionSummaryDTO>> listAuctions(ListAuctionRequest request)
      throws Exception {
    List<AuctionSummaryDTO> list = auctionQueryService.listAuctions(request);
    return Response.success("Auctions retrieved successfully.", list);
  }

  public Response<Boolean> setBidStep(SetBidStepRequest request) throws Exception {
    validateRequiredText(request.getAuctionId(), "Auction ID is required.");
    validateRequiredText(request.getUserId(), "Seller ID is required.");
    if (request.getBidStep() <= 0) {
      throw new ValidationException("Bid step must be greater than 0.");
    }

    boolean success = auctionService.setBidStep(request);
    if (success) {
      broadcastService.broadcastBidStepUpdated(request.getAuctionId(), request.getBidStep());
    }
    return Response.success("Bid step updated successfully.", success);
  }


  // HELPER
  private static void validateRequiredText(String value, String message)
      throws ValidationException {
    if (value == null || value.isBlank()) {
      throw new ValidationException(message);
    }
  }
}

