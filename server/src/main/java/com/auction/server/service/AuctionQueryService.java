package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.share.DTO.AuctionDetailDTO;
import com.auction.share.DTO.AuctionSummaryDTO;
import com.auction.share.DTO.BidDTO;
import com.auction.share.DTO.ListAuctionRequest;
import com.auction.share.enums.AuctionStatus;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.auction.BidTransaction;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuctionQueryService {
    private final AuctionDAO auctionDAO;
    private final BidTransactionDAO bidTransactionDAO;

    public AuctionQueryService(AuctionDAO auctionDAO, BidTransactionDAO bidTransactionDAO) {
        this.auctionDAO = auctionDAO;
        this.bidTransactionDAO = bidTransactionDAO;
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

    public AuctionDetailDTO getAuctionDetail(String auctionId) throws SQLException, ValidationException {
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

        String highestBidderName =
                auction.getHighestBidder() != null ? auction.getHighestBidder().getFullName() : null;
        String highestBidderUsername =
                auction.getHighestBidder() != null ? auction.getHighestBidder().getUsername() : null;

        return new AuctionDetailDTO(
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

    public List<AuctionSummaryDTO> listAuctions(ListAuctionRequest req) throws SQLException {
        List<Auction> auctions = resolveAuctionsByFilter(req);
        if (auctions.isEmpty()) return new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

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

    private List<Auction> resolveAuctionsByFilter(ListAuctionRequest req) throws SQLException {
        if (req == null) {
            return auctionDAO.findAll().stream()
                    .filter(a -> a.getStatus() != AuctionStatus.CANCELED)
                    .collect(Collectors.toList());
        }

        String sellerId = req.getSellerId();
        String statusStr = req.getStatus();
        boolean hasSeller = sellerId != null && !sellerId.isBlank();
        boolean hasStatus = statusStr != null && !statusStr.isBlank();

        AuctionStatus status = null;
        if (hasStatus) {
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

        if (!hasSeller) {
            result = result.stream()
                    .filter(a -> a.getStatus() != AuctionStatus.CANCELED)
                    .collect(Collectors.toList());
        }

        return result;
    }
}
