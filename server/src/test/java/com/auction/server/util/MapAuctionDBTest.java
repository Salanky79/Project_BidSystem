package com.auction.server.util;

import com.auction.share.enums.AuctionStatus;
import com.auction.share.enums.Category;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapAuctionDBTest {

    @Test
    void mapItemShouldMapKnownCategory() throws SQLException {
        ResultSet rs = resultSetOf(Map.of(
                "id", "item-1",
                "seller_id", "seller-1",
                "name", "Vintage Watch",
                "category", "jewelry",
                "starting_price", 1000.0,
                "description", "Good condition"
        ));

        Item item = MapAuctionDB.mapItem(rs);

        assertEquals("item-1", item.getId());
        assertEquals("Vintage Watch", item.getName());
        assertEquals(Category.JEWELRY, item.getCategory());
    }

    @Test
    void mapItemShouldFallbackToItemCategoryWhenInvalid() throws SQLException {
        ResultSet rs = resultSetOf(Map.of(
                "id", "item-2",
                "seller_id", "seller-2",
                "name", "Unknown",
                "category", "not_a_category",
                "starting_price", 10.0,
                "description", "desc"
        ));

        Item item = MapAuctionDB.mapItem(rs);

        assertEquals(Category.ITEM, item.getCategory());
    }

    @Test
    void mapAuctionShouldMapStatusTimeAndHighestBid() throws SQLException {
        LocalDateTime start = LocalDateTime.of(2026, 5, 21, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 21, 10, 0);

        ResultSet rs = resultSetOf(Map.of(
                "id", "auction-1",
                "start_time", Timestamp.valueOf(start),
                "end_time", Timestamp.valueOf(end),
                "status", "RUNNING",
                "current_price", 1550.0
        ));

        Item item = new Item("Item", "Desc", 1000.0, "seller-1", Category.ITEM);
        Seller seller = new Seller("seller", "pwd", "Seller", "090", "s@mail.com", "HCM");
        Bidder bidder = new Bidder("bidder", "pwd", "Bidder", "091", "b@mail.com", "HN");

        Auction auction = MapAuctionDB.mapAuction(rs, item, seller, bidder);

        assertEquals("auction-1", auction.getId());
        assertEquals(start, auction.getStartTime());
        assertEquals(end, auction.getEndTime());
        assertEquals(AuctionStatus.RUNNING, auction.getStatus());
        assertEquals(1550.0, auction.getCurrentHighestBid());
        assertSame(bidder, auction.getHighestBidder());
    }

    @Test
    void mapAuctionShouldKeepOpenWhenStatusNullAndNoHighestBidder() throws SQLException {
        LocalDateTime start = LocalDateTime.of(2026, 5, 21, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 21, 10, 0);
        ResultSet rs = resultSetOf(Map.of(
                "id", "auction-2",
                "start_time", java.sql.Timestamp.valueOf(start),
                "end_time", java.sql.Timestamp.valueOf(end),
                "current_price", 1000.0
        ));

        Item item = new Item("Item", "Desc", 1000.0, "seller-1", Category.ITEM);
        Seller seller = new Seller("seller", "pwd", "Seller", "090", "s@mail.com", "HCM");

        Auction auction = MapAuctionDB.mapAuction(rs, item, seller, null);

        assertEquals(AuctionStatus.OPEN, auction.getStatus());
        assertNull(auction.getHighestBidder());
        assertEquals(item.getStartingPrice(), auction.getCurrentHighestBid());
    }

    @Test
    void mapAuctionShouldAllowNullTimes() throws SQLException {
        ResultSet rs = resultSetOf(Map.of(
                "id", "auction-3",
                "status", "OPEN",
                "current_price", 1000.0
        ));

        Item item = new Item("Item", "Desc", 1000.0, "seller-1", Category.ITEM);
        Seller seller = new Seller("seller", "pwd", "Seller", "090", "s@mail.com", "HCM");

        Auction auction = MapAuctionDB.mapAuction(rs, item, seller, null);

        assertNull(auction.getStartTime());
        assertNull(auction.getEndTime());
    }

    private static ResultSet resultSetOf(Map<String, Object> values) {
        Map<String, Object> safeValues = new HashMap<>(values);
        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getString".equals(name)) {
                        Object value = safeValues.get((String) args[0]);
                        return value == null ? null : String.valueOf(value);
                    }
                    if ("getDouble".equals(name)) {
                        Object value = safeValues.get((String) args[0]);
                        return value == null ? 0.0 : ((Number) value).doubleValue();
                    }
                    if ("getTimestamp".equals(name)) {
                        return safeValues.get((String) args[0]);
                    }
                    throw new UnsupportedOperationException("Method not supported in test ResultSet: " + name);
                }
        );
    }
}
