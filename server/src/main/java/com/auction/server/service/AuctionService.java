package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.share.DTO.CreateAuctionRequest;
import com.auction.share.DTO.SetBidStepRequest;
import com.auction.share.enums.AuctionStatus;
import com.auction.share.enums.Category;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Dịch vụ xử lý Command liên quan đến phiên đấu giá (tạo, hủy, thay đổi cấu hình).
 */
public class AuctionService {
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

        itemDAO.saveItem(item);
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


    private LocalDateTime parseDateTime(String dateTimeStr) throws ValidationException {
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Please use ISO format.");
        }
    }
}
