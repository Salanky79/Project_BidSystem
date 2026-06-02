package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.share.DTO.AuctionDetailDTO;
import com.auction.share.DTO.AuctionSummaryDTO;
import com.auction.share.DTO.ListAuctionRequest;
import com.auction.share.enums.AuctionStatus;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.auction.BidTransaction;
import com.auction.server.mapper.AuctionMapper;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuctionQueryService {
    private final DataSource dataSource;
    private final AuctionDAO auctionDAO;
    private final BidTransactionDAO bidTransactionDAO;

    public AuctionQueryService(DataSource dataSource, AuctionDAO auctionDAO, BidTransactionDAO bidTransactionDAO) {
        this.dataSource = dataSource;
        this.auctionDAO = auctionDAO;
        this.bidTransactionDAO = bidTransactionDAO;
    }

    public Auction getAuctionById(String auctionId) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return auctionDAO.findById(conn, auctionId);
        }
    }



    public AuctionDetailDTO getAuctionDetail(String auctionId) throws SQLException, ValidationException {
        try (Connection conn = dataSource.getConnection()) {
            Auction auction = auctionDAO.findById(conn, auctionId);
            if (auction == null) {
                throw new ValidationException("Auction not found.");
            }

            List<BidTransaction> transactions = bidTransactionDAO.findByAuction(conn, auction);
            return AuctionMapper.toDetailDTO(auction, transactions);
        }
    }

    public List<AuctionSummaryDTO> listAuctions(ListAuctionRequest req) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            List<Auction> auctions = resolveAuctionsByFilter(conn, req);
            if (auctions.isEmpty()) return new ArrayList<>();

            List<AuctionSummaryDTO> summaries = new ArrayList<>();
            for (Auction auction : auctions) {
                summaries.add(AuctionMapper.toSummaryDTO(auction));
            }
            return summaries;
        }
    }

    private List<Auction> resolveAuctionsByFilter(Connection conn, ListAuctionRequest req) throws SQLException {
        String sellerId = req.getSellerId();
        if ((sellerId == null || sellerId.isBlank()) && req.isSellerOnly()) {
            sellerId = req.getUserId();
        }
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
            result = auctionDAO.findBySellerAndStatus(conn, sellerId, status);
        } else if (hasSeller) {
            result = auctionDAO.findBySeller(conn, sellerId);
        } else if (hasStatus) {
            result = auctionDAO.findByStatus(conn, status);
        } else {
            result = auctionDAO.findAll(conn);
        }

        if (!hasSeller) {
            result = result.stream()
                    .filter(a -> a.getStatus() != AuctionStatus.CANCELED)
                    .collect(Collectors.toList());
        }

        return result;
    }
}
