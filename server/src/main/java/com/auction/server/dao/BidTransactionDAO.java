package com.auction.server.dao;

import com.auction.share.DTO.ProfileBidTransactionDTO;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.auction.BidTransaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface BidTransactionDAO {
    boolean saveBidTransaction(Connection conn, BidTransaction transaction) throws SQLException;
    List<BidTransaction> findByAuction(Connection conn, Auction auction) throws SQLException;
    List<ProfileBidTransactionDTO> findProfileTransactionsByBidderId(Connection conn, String bidderId) throws SQLException;
}
