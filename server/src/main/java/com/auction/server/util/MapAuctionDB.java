package com.auction.server.util;

import com.auction.share.enums.AuctionStatus;
import com.auction.share.enums.Category;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.*;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public final class MapAuctionDB {

    public static Item mapItem(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String sellerId = rs.getString("seller_id");
        String name = rs.getString("name");
        String categoryRaw = rs.getString("category");
        double startingPrice = rs.getDouble("starting_price");
        String description = rs.getString("description");
        
        Item item;
        Category category;
        try {
            category = categoryRaw == null ? Category.ITEM : Category.valueOf(categoryRaw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            category = Category.ITEM;
        }
        
        switch (category) {
            case ANTIQUE:
                item = new Antique(name, description, startingPrice, sellerId, 
                        rs.getString("era"), rs.getString("material"));
                break;
            case ART:
                item = new Art(name, description, startingPrice, sellerId, 
                        rs.getString("artist"), rs.getInt("year"));
                break;
            case ELECTRONIC:
                item = new Electronic(name, description, startingPrice, sellerId, 
                        rs.getString("brand"), rs.getInt("warranty_period"));
                break;
            case JEWELRY:
                item = new Jewelry(name, description, startingPrice, sellerId, 
                        rs.getString("material"), rs.getDouble("caratWeight"));
                break;
            case REALESTATE:
                item = new RealEstate(name, description, startingPrice, sellerId, 
                        rs.getString("location"), rs.getDouble("area"));
                break;
            case VEHICLE:
                item = new Vehicle(name, description, startingPrice, sellerId, 
                         rs.getString("fuelType"));
                break;
            default:
                item = new Item(name, description, startingPrice, sellerId);
                break;
        }
        item.setID(id);
        return item;
    }

    public static Auction mapAuction(ResultSet rs, Item item, Seller seller, Bidder highestBidder) throws SQLException {
        String id = rs.getString("id");
        Timestamp startTimestamp = rs.getTimestamp("start_time");
        Timestamp endTimestamp = rs.getTimestamp("end_time");
        
        LocalDateTime startTime = startTimestamp != null ? startTimestamp.toLocalDateTime() : null;
        LocalDateTime endTime = endTimestamp != null ? endTimestamp.toLocalDateTime() : null;
        
        Auction auction = new Auction(item, seller, startTime, endTime);
        auction.setID(id);
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            switch (AuctionStatus.valueOf(statusStr)) {
                case RUNNING:
                    auction.markRunning();
                    break;
                case FINISHED:
                    auction.markFinished();
                    break;
                case CANCELED:
                    auction.markCanceled();
                    break;
                case OPEN:
                default:
                    break;
            }
        }
        
        double currentPrice = rs.getDouble("current_price");
        if (highestBidder != null) {
            auction.setHighestBid(highestBidder, currentPrice);
        }
        
        return auction;
    }
}
