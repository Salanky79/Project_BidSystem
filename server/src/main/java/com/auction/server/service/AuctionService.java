package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.util.ImageStorage;
import com.auction.share.DTO.CreateAuctionRequest;
import com.auction.share.DTO.SetBidStepRequest;
import com.auction.share.enums.AuctionStatus;
import com.auction.share.enums.Category;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;

import java.sql.Connection;
import com.auction.server.util.DatabaseConnection;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Dịch vụ xử lý Command liên quan đến phiên đấu giá (tạo, hủy, thay đổi cấu hình).
 */
public class AuctionService implements IAuctionService {
    private final AuctionDAO auctionDAO;
    private final ItemDAO itemDAO;
    private final UserDAO userDAO;
    private ImageStorage imageStorage;

    public AuctionService(
            AuctionDAO auctionDAO,
            ItemDAO itemDAO,
            UserDAO userDAO) {
        this.auctionDAO = auctionDAO;
        this.itemDAO = itemDAO;
        this.userDAO = userDAO;
    }

    public void setImageStorage(ImageStorage imageStorage) {
        this.imageStorage = imageStorage;
    }

    public Auction createAuction(CreateAuctionRequest req) throws SQLException, ValidationException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            User sellerUser = userDAO.findById(conn, req.getSellerId());
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

            // Upload image to ImageStorage if provided
            if (req.getImageBytes() != null && req.getImageBytes().length > 0 && imageStorage != null) {
                try {
                    String imageUrl = imageStorage.uploadImage(req.getImageBytes(), req.getImageName());
                    item.setImageUrl(imageUrl);
                } catch (Exception e) {
                    // Cho phép tiếp tục tạo đấu giá kể cả khi upload ảnh thất bại để tránh chặn người dùng
                }
            }

            Auction auction = new Auction(item, seller, startTime, endTime);

            conn.setAutoCommit(false);
            try {
                itemDAO.saveItem(conn, item);
                auctionDAO.saveAuction(conn, auction);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            return auction;
        }
    }

    public void cancelAuction(String auctionId, String sellerId) throws SQLException, ValidationException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Auction auction = auctionDAO.findById(conn, auctionId);
            if (auction == null || !auction.getSeller().getId().equals(sellerId)) {
                throw new ValidationException("Auction not found or you are not the seller.");
            }
            if (auction.getStatus() != AuctionStatus.OPEN) {
                throw new ValidationException("Can only cancel OPEN auctions.");
            }
            auctionDAO.updateStatus(conn, auctionId, AuctionStatus.CANCELED);
        }
    }

    public int finishAuctions() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            conn.setAutoCommit(false);
            try {
                // 1. Trừ tiền bidder
                userDAO.deductWinningBidders(conn, now);
                // 2. Cộng tiền seller
                userDAO.creditSellers(conn, now);
                // 3. Đổi trạng thái auction
                int finishedRows = auctionDAO.finishAuctions(conn, now);
                conn.commit();
                return finishedRows;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean setBidStep(SetBidStepRequest req) throws SQLException, ValidationException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Auction auction = auctionDAO.findById(conn, req.getAuctionId());
            if (auction == null) {
                throw new ValidationException("Auction not found.");
            }
            if (!auction.getSeller().getId().equals(req.getSellerId())) {
                throw new ValidationException("You are not allowed to update this auction.");
            }
            if (req.getBidStep() <= 0) {
                throw new ValidationException("Bid step must be greater than 0.");
            }
            return auctionDAO.updateBidStep(conn, req.getAuctionId(), req.getBidStep());
        }
    }


    private LocalDateTime parseDateTime(String dateTimeStr) throws ValidationException {
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Please use ISO format.");
        }
    }
}
