package com.auction.server.mapper;

import com.auction.share.enums.AuctionStatus;
import com.auction.share.enums.Category;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Tiện ích hỗ trợ ánh xạ (mapping) dữ liệu từ ResultSet (Database) thành đối tượng Item, Auction.
 */
public final class MapAuctionDB {

  public static Item mapItem(ResultSet rs) throws SQLException {
    String id = rs.getString("id");
    String sellerId = rs.getString("seller_id");
    String name = rs.getString("name");
    String categoryRaw = rs.getString("category");
    double startingPrice = rs.getDouble("starting_price");
    String description = rs.getString("description");
    String imageUrl = rs.getString("image_url");

    Category category;
    try {
      category = Category.valueOf(categoryRaw.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      category = Category.ITEM;
    }

    Item item = new Item(name, description, startingPrice, sellerId, category);
    item.setID(id);
    item.setImageUrl(imageUrl);
    return item;
  }

  public static Auction mapAuction(ResultSet rs, Item item, Seller seller, Bidder highestBidder)
      throws SQLException {
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
    double bidStep = rs.getDouble("bid_step");
    auction.setBidStep(bidStep);
    if (highestBidder != null) {
      auction.setHighestBid(highestBidder, currentPrice);
    }

    return auction;
  }
}
