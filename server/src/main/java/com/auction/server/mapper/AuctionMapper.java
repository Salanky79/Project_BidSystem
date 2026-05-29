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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.auction.share.DTO.AuctionSummaryDTO;
import com.auction.share.DTO.AuctionDetailDTO;
import com.auction.share.DTO.BidDTO;
import com.auction.share.models.auction.BidTransaction;

/**
 * Tiện ích hỗ trợ ánh xạ (mapping) dữ liệu từ ResultSet (Database) thành đối tượng Item, Auction.
 */
public final class AuctionMapper {

  public static Item extractItemFromDB(ResultSet rs) throws SQLException {
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

  public static Auction extractAuctionFromDB(ResultSet rs) throws SQLException {
      String id = rs.getString("id");
      String sellerId = rs.getString("seller_id");

      // Map Item
      String itemId = rs.getString("item_id");
      String itemName = rs.getString("item_name");
      String itemDescription = rs.getString("item_description");
      double itemStartingPrice = rs.getDouble("item_starting_price");
      String itemCategory = rs.getString("item_category");
      Category category;
      try {
          category = Category.valueOf(itemCategory.trim().toUpperCase());
      } catch (IllegalArgumentException e) {
          category = Category.ITEM;
      }
      Item item = new Item(itemName, itemDescription, itemStartingPrice, sellerId, category);
      item.setID(itemId);
      item.setImageUrl(rs.getString("item_image_url"));

      // Map Seller
      String sellerUsername = rs.getString("seller_username");
      String sellerFullName = rs.getString("seller_fullname");
      String sellerPhone = rs.getString("seller_phoneNumber");
      String sellerEmail = rs.getString("seller_email");
      String sellerAddress = rs.getString("seller_address");
      double sellerBalance = rs.getDouble("seller_balance");
      Seller seller = new Seller(sellerUsername, null, sellerFullName, sellerPhone, sellerEmail, sellerAddress);
      seller.setID(sellerId);
      seller.setBalance(sellerBalance);

      // Map Highest Bidder
      String bidderId = rs.getString("highest_bidder_id");
      Bidder highestBidder = null;
      if (bidderId != null) {
          String bidderUsername = rs.getString("bidder_username");
          String bidderFullName = rs.getString("bidder_fullname");
          String bidderPhone = rs.getString("bidder_phoneNumber");
          String bidderEmail = rs.getString("bidder_email");
          String bidderAddress = rs.getString("bidder_address");
          double bidderBalance = rs.getDouble("bidder_balance");
          highestBidder = new Bidder(bidderUsername, null, bidderFullName, bidderPhone, bidderEmail, bidderAddress);
          highestBidder.setID(bidderId);
          highestBidder.setBalance(bidderBalance);
      }

      Timestamp startTimestamp = rs.getTimestamp("start_time");
      Timestamp endTimestamp = rs.getTimestamp("end_time");
      LocalDateTime startTime = startTimestamp != null ? startTimestamp.toLocalDateTime() : null;
      LocalDateTime endTime = endTimestamp != null ? endTimestamp.toLocalDateTime() : null;

      Auction auction = new Auction(item, seller, startTime, endTime);
      auction.setID(id);

      String statusStr = rs.getString("status");
      if (statusStr != null) {
          try {
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
          } catch (IllegalArgumentException ignored) {}
      }

      double currentPrice = rs.getDouble("current_price");
      double bidStep = rs.getDouble("bid_step");
      auction.setBidStep(bidStep);
      if (highestBidder != null) {
          auction.setHighestBid(highestBidder, currentPrice);
      }

      // Map bid_count (if present in the ResultSet)
      auction.setBidCount(rs.getInt("bid_count"));

      return auction;
  }

  public static AuctionSummaryDTO toSummaryDTO(Auction auction) {
      if (auction == null) return null;
      return new AuctionSummaryDTO(
          auction.getId(),
          auction.getItem().getName(),
          auction.getItem().getCategory().name(),
          auction.getCurrentHighestBid(),
          auction.getBidStep(),
          auction.getStatus().name(),
          auction.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
          auction.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
          auction.getBidCount(),
          auction.getItem().getImageUrl()
      );
  }

  public static AuctionDetailDTO toDetailDTO(Auction auction, List<BidTransaction> transactions) {
      if (auction == null) return null;
      if (auction.getItem() == null || auction.getSeller() == null) {
          throw new IllegalArgumentException("Auction must have a valid item and seller");
      }
      List<BidDTO> bidHistory = new ArrayList<>();
      DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
      if (transactions != null) {
          for (BidTransaction tx : transactions) {
              bidHistory.add(new BidDTO(tx.getBidder().getFullName(), tx.getAmount(), tx.getTimestamp().format(formatter)));
          }
      }
      String highestBidderName = auction.getHighestBidder() != null ? auction.getHighestBidder().getFullName() : null;
      String highestBidderUsername = auction.getHighestBidder() != null ? auction.getHighestBidder().getUsername() : null;

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
          auction.getBidCount(),
          auction.getItem().getImageUrl()
      );
  }
}