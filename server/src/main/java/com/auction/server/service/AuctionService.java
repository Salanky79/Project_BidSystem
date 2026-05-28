package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.service.CloudinaryService;
import com.auction.server.util.DatabaseConnection;
import com.auction.share.DTO.AuctionDetailDTO;
import com.auction.share.DTO.AuctionSummaryDTO;
import com.auction.share.DTO.BidDTO;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.DTO.CreateAuctionRequest;
import com.auction.share.DTO.ExtendEndTimeRequest;
import com.auction.share.DTO.ListAuctionRequest;
import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.DTO.SetBidStepRequest;
import com.auction.share.enums.AuctionStatus;
import com.auction.share.enums.Category;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.auction.BidTransaction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dịch vụ xử lý nghiệp vụ cốt lõi của các phiên đấu giá (tạo, hủy, đặt giá, kiểm tra trạng thái).
 */
public class AuctionService {
  private final AuctionDAO auctionDAO;
  private final ItemDAO itemDAO;
  private final BidTransactionDAO bidTransactionDAO;
  private final UserDAO userDAO;
  private final BidBroadcastService bidBroadcastService;
  private AutoBidService autoBidService;
  private CloudinaryService cloudinaryService;

  public AuctionService(
      AuctionDAO auctionDAO,
      ItemDAO itemDAO,
      BidTransactionDAO bidTransactionDAO,
      UserDAO userDAO,
      BidBroadcastService bidBroadcastService,
      AutoBidService autoBidService) {
    this.auctionDAO = auctionDAO;
    this.itemDAO = itemDAO;
    this.bidTransactionDAO = bidTransactionDAO;
    this.userDAO = userDAO;
    this.bidBroadcastService = bidBroadcastService;
    this.autoBidService = autoBidService;
  }

  public AuctionService(
      AuctionDAO auctionDAO,
      ItemDAO itemDAO,
      BidTransactionDAO bidTransactionDAO,
      UserDAO userDAO,
      BidBroadcastService bidBroadcastService) {
    this(auctionDAO, itemDAO, bidTransactionDAO, userDAO, bidBroadcastService, null);
  }

  public void setAutoBidService(AutoBidService autoBidService) {
    this.autoBidService = autoBidService;
  }

  public void setCloudinaryService(CloudinaryService cloudinaryService) {
    this.cloudinaryService = cloudinaryService;
  }

  public Auction createAuction(CreateAuctionRequest req) throws SQLException, ValidationException {
    User sellerUser = userDAO.findById(req.getSellerId());
    if (!(sellerUser instanceof Seller seller)) {
      throw new ValidationException("User is not a seller.");
    }

    LocalDateTime startTime = parseDateTime(req.getStartTime());
    LocalDateTime endTime = parseDateTime(req.getEndTime());
    if (!startTime.isBefore(endTime)) {
      throw new ValidationException("Start time must be before end time.");
    }

    Category category = Category.valueOf(req.getCategory().trim().toUpperCase());
    Item item =
        new Item(
            req.getItemName(),
            req.getDescription(),
            req.getStartingPrice(),
            req.getSellerId(),
            category);

    // Upload image to Cloudinary if provided
    if (req.getImageBytes() != null && req.getImageBytes().length > 0) {
      try {
        String imageUrl = cloudinaryService.uploadImage(req.getImageBytes(), req.getImageName());
        item.setImageUrl(imageUrl);
      } catch (Exception e) {
        System.err.println("Failed to upload image to Cloudinary: " + e.getMessage());
        // Cho phép tiếp tục tạo đấu giá kể cả khi upload ảnh thất bại để tránh chặn người dùng
      }
    }

    Auction auction = new Auction(item, seller, startTime, endTime);

    itemDAO.saveItem(item); // lưu vào DB
    auctionDAO.saveAuction(auction);
    return auction;
  }

  public boolean cancelAuction(String auctionId) throws SQLException, ValidationException {
    Auction auction = auctionDAO.findById(auctionId);
    if (auction == null) {
      throw new ValidationException("Auction not found.");
    }
    if (auction.getStatus() == AuctionStatus.FINISHED
        || auction.getStatus() == AuctionStatus.CANCELED) {
      throw new ValidationException(
          "Cannot cancel an auction that is already finished or cancelled.");
    }

    return auctionDAO.updateStatus(auctionId, AuctionStatus.CANCELED);
  }

  public boolean placeBid(PlaceBidRequest req) throws SQLException, ValidationException {
    return placeBidInternal(req, true);
  }

  public boolean placeBidInternal(PlaceBidRequest req, boolean triggerAutoBid)
      throws SQLException, ValidationException {
    Auction auction = auctionDAO.findById(req.getAuctionId());
    if (!isAuctionRunning(auction)) {
      throw new ValidationException(
          auction == null ? "Auction not found." : "Auction is not running.");
    }

    Bidder bidder = requireBidder(req.getBidderId());
    if (req.getAmount() <= auction.getCurrentHighestBid()) {
      throw new ValidationException("Bid amount must be higher than current highest bid.");
    }
    if (req.getAmount() < auction.getCurrentHighestBid() + auction.getBidStep()) {
      throw new ValidationException("Bid amount must be at least current price + bid step.");
    }

    placeBidAndBroadcast(auction, bidder, req.getAmount());
    if (triggerAutoBid && autoBidService != null) {
      autoBidService.triggerAutoBid(req.getAuctionId(), bidder.getId());
    }
    return true;
  }

  protected Connection getConnection() throws SQLException {
    return DatabaseConnection.getConnection();
  }

  public Auction getAuctionById(String auctionId) throws SQLException {
    return auctionDAO.findById(auctionId);
  }

  public boolean isAuctionRunning(Auction auction) {
    if (auction == null) {
      return false;
    }
    LocalDateTime now = LocalDateTime.now();
    return !(now.isAfter(auction.getEndTime()) || now.isBefore(auction.getStartTime()));
  }

  public Bidder requireBidder(String bidderId) throws SQLException, ValidationException {
    User bidderUser = userDAO.findById(bidderId);
    if (!(bidderUser instanceof Bidder bidder)) {
      throw new ValidationException("User is not a bidder.");
    }
    return bidder;
  }

  public AuctionDetailDTO getAuctionDetail(String auctionId)
      throws SQLException, ValidationException {
    Auction auction = auctionDAO.findById(auctionId);
    if (auction == null) {
      throw new ValidationException("Auction not found.");
    }

    List<BidTransaction> transactions = bidTransactionDAO.findByAuction(auction);
    List<BidDTO> bidHistory = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    for (BidTransaction tx : transactions) {
      bidHistory.add(
          new BidDTO(
              tx.getBidder().getFullName(), tx.getAmount(), tx.getTimestamp().format(formatter)));
    }

    // tách ra để tránh lỗi null
    // Seller
    String highestBidderName =
        auction.getHighestBidder() != null ? auction.getHighestBidder().getFullName() : null;
    // HighestBidder
    String highestBidderUsername =
        auction.getHighestBidder() != null ? auction.getHighestBidder().getUsername() : null;

    return new AuctionDetailDTO(
            // item
        auction.getId(),
        auction.getItem().getName(),
        auction.getItem().getDescription(),
        auction.getItem().getCategory().name(),
        auction.getSeller().getFullName(),
        auction.getItem().getStartingPrice(),
        auction.getCurrentHighestBid(),
        auction.getBidStep(),
        auction.getStatus().name(),
        auction.getStartTime().format(formatter),
        auction.getEndTime().format(formatter),

        highestBidderName,
        highestBidderUsername,
        bidHistory,
        auction.getItem().getImageUrl());
  }

  // lấy danh sách auction
  public List<AuctionSummaryDTO> listAuctions(ListAuctionRequest req) throws SQLException {
    // lấy từ DB
    List<Auction> auctions = resolveAuctionsByFilter(req);
    if (auctions.isEmpty()) return new ArrayList<>();

    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Lấy toàn bộ số lượt auctionID bằng 1 query GROUP BY thay vì N query riêng lẻ
    List<String> ids = auctions.stream().map(Auction::getId).collect(Collectors.toList());
    Map<String, Integer> bidCounts = bidTransactionDAO.countByAuctionIds(ids);

    List<AuctionSummaryDTO> summaries = new ArrayList<>();
    for (Auction auction : auctions) {
      int bidCount = bidCounts.getOrDefault(auction.getId(), 0);
      summaries.add(
          new AuctionSummaryDTO(
              auction.getId(),
              auction.getItem().getName(),
              auction.getItem().getCategory().name(),
              auction.getCurrentHighestBid(),
              auction.getBidStep(),
              auction.getStatus().name(),
              auction.getStartTime().format(formatter),
              auction.getEndTime().format(formatter),
              bidCount,
              auction.getItem().getImageUrl()));
    }
    return summaries;
  }

  public boolean setBidStep(SetBidStepRequest req) throws SQLException, ValidationException {
    Auction auction = auctionDAO.findById(req.getAuctionId());
    if (auction == null) {
      throw new ValidationException("Auction not found.");
    }
    if (!auction.getSeller().getId().equals(req.getSellerId())) {
      throw new ValidationException("You are not allowed to update this auction.");
    }
    if (req.getBidStep() <= 0) {
      throw new ValidationException("Bid step must be greater than 0.");
    }
    return auctionDAO.updateBidStep(req.getAuctionId(), req.getBidStep());
  }

  public boolean extendEndTime(ExtendEndTimeRequest req) throws SQLException, ValidationException {
    Auction auction = auctionDAO.findById(req.getAuctionId());
    if (auction == null) {
      throw new ValidationException("Auction not found.");
    }
    if (!auction.getSeller().getId().equals(req.getSellerId())) {
      throw new ValidationException("You are not allowed to update this auction.");
    }
    if (req.getMinutes() <= 0) {
      throw new ValidationException("Minutes must be greater than 0.");
    }

    LocalDateTime newEndTime = auction.getEndTime().plusMinutes(req.getMinutes());
    return auctionDAO.updateEndTime(req.getAuctionId(), newEndTime);
  }

  private void placeBidAndBroadcast(Auction auction, Bidder bidder, double amount)
      throws SQLException, ValidationException {
    BidTransaction transaction = new BidTransaction(auction, bidder, amount);

    try (Connection conn = getConnection()) {
      conn.setAutoCommit(false);
      try {
        boolean updated = auctionDAO.updateHighestBidIfHigher(conn, auction.getId(), bidder.getId(), amount);
        if (!updated) {
          conn.rollback();
          throw new ValidationException(
              "Bid rejected: auction is not running, already ended, insufficient balance, or"
                  + " current price changed.");
        }

        bidTransactionDAO.saveBidTransaction(conn, transaction);
        conn.commit();
        bidBroadcastService.broadcastBidUpdate(
            new BidUpdateEvent(
                BidBroadcastService.BID_UPDATED,
                auction.getId(),
                bidder.getId(),
                bidder.getFullName(),
                amount,
                amount,
                transaction.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
      } catch (SQLException | ValidationException e) {
        conn.rollback();
        throw e;
      } finally {
        conn.setAutoCommit(true);
      }
    }
  }

  private LocalDateTime parseDateTime(String dateTimeStr) throws ValidationException {
    try {
      return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    } catch (DateTimeParseException e) {
      throw new ValidationException("Invalid date format. Please use ISO format.");
    }
  }


  // filter lấy những auction nào từ DB
  private List<Auction> resolveAuctionsByFilter(ListAuctionRequest req) throws SQLException {
    // CASE 1: lấy toàn bộ auction (trừ cái đã bị hủy)
    if (req == null) {

      return auctionDAO.findAll().stream()
          .filter(a -> a.getStatus() != AuctionStatus.CANCELED)
          .collect(java.util.stream.Collectors.toList());
    }

    String sellerId = req.getSellerId();
    String statusStr = req.getStatus();
    boolean hasSeller = sellerId != null && !sellerId.isBlank();
    boolean hasStatus = statusStr != null && !statusStr.isBlank();

    AuctionStatus status = null;
    if (hasStatus) {
      // convert STATUS
      try {
        status = AuctionStatus.valueOf(statusStr.trim().toUpperCase());
      } catch (IllegalArgumentException e) {
        hasStatus = false;
      }
    }

    List<Auction> result;
    if (hasSeller && hasStatus) {
      result = auctionDAO.findBySellerAndStatus(sellerId, status);
    } else if (hasSeller) {
      result = auctionDAO.findBySeller(sellerId);
    } else if (hasStatus) {
      result = auctionDAO.findByStatus(status);
    } else {
      result = auctionDAO.findAll();
    }

    // Nếu không tìm kiếm theo một seller cụ thể (lấy danh sách chung cho trang chủ), lọc bỏ CANCELED
    if (!hasSeller) {
      result = result.stream()
          .filter(a -> a.getStatus() != AuctionStatus.CANCELED)
          .collect(java.util.stream.Collectors.toList());
    }

    return result;
  }
}
