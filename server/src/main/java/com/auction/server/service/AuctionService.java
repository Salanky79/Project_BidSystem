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
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dịch vụ xử lý Command liên quan đến phiên đấu giá (tạo, hủy, thay đổi cấu hình).
 */
public class AuctionService  {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionService.class);
    private final DataSource dataSource;
    private final AuctionDAO auctionDAO;
    private final ItemDAO itemDAO;
    private final UserDAO userDAO;
    private ImageStorage imageStorage;

    public AuctionService(
            DataSource dataSource,
            AuctionDAO auctionDAO,
            ItemDAO itemDAO,
            UserDAO userDAO,
            ImageStorage imageStorage) {
        this.dataSource = dataSource;
        this.auctionDAO = auctionDAO;
        this.itemDAO = itemDAO;
        this.userDAO = userDAO;
        this.imageStorage = imageStorage;
    }

    public Auction createAuction(CreateAuctionRequest req) throws SQLException, ValidationException {
        try (Connection conn = dataSource.getConnection()) {
            User sellerUser = userDAO.findById(conn, req.getUserId());
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
                            req.getUserId(),
                            category);

            // Upload image to ImageStorage if provided
            if (req.getImageBytes() != null && req.getImageBytes().length > 0 && imageStorage != null) {
                try {
                    String imageUrl = imageStorage.uploadImage(req.getImageBytes(), req.getImageName());
                    item.setImageUrl(imageUrl);
                } catch (Exception e) {
                    LOGGER.warn("Image upload failed for item={}, auction will be created without image. Error: {}",
                        req.getItemName(), e.getMessage());
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
        try (Connection conn = dataSource.getConnection()) {
            Auction auction = auctionDAO.findById(conn, auctionId);
            if (auction == null || !auction.getSeller().getId().equals(sellerId)) {
                throw new ValidationException("Auction not found or you are not the seller.");
            }
            if (auction.getStatus() != AuctionStatus.OPEN && auction.getStatus() != AuctionStatus.RUNNING) {
                throw new ValidationException("Can only cancel OPEN or RUNNING auctions.");
            }
                auctionDAO.updateStatus(conn, auctionId, AuctionStatus.CANCELED);
        }
    }

    public void markRunningAuctions() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            auctionDAO.markOpenAuctionsAsRunning(conn);
        }
    }

    public List<String> settleFinishedAuctions() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                List<String> endedIds = auctionDAO.findEndedRunningAuctionIds(conn, now.toLocalDateTime());
                if (endedIds.isEmpty()) {
                    conn.commit();
                    return endedIds;
                }
                userDAO.deductWinningBidders(conn, endedIds);
                userDAO.creditSellers(conn, endedIds);
                auctionDAO.finishAuctions(conn, endedIds);
                conn.commit();
                return endedIds;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean setBidStep(SetBidStepRequest req) throws SQLException, ValidationException {
        try (Connection conn = dataSource.getConnection()) {
            Auction auction = auctionDAO.findById(conn, req.getAuctionId());
            if (auction == null) {
                throw new ValidationException("Auction not found.");
            }
            if (!auction.getSeller().getId().equals(req.getUserId())) {
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

