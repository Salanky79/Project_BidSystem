package com.auction.server.dao;

import com.auction.share.models.item.Item;

import java.sql.Connection;
import java.sql.SQLException;

public interface ItemDAO {
    boolean saveItem(Connection conn, Item item) throws SQLException;
    Item findById(Connection conn, String id) throws SQLException;
}
