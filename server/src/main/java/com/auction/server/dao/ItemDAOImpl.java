package com.auction.server.dao;

import com.auction.server.mapper.AuctionMapper;
import com.auction.share.models.item.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemDAOImpl implements ItemDAO {
    
    public boolean saveItem(Connection conn, Item item) throws SQLException {
        String sql = "INSERT INTO items (id, seller_id, name, category, starting_price, description, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, item.getId());
            ps.setString(2, item.getSellerId());
            ps.setString(3, item.getName());
            ps.setString(4, item.getCategory().name());
            ps.setDouble(5, item.getStartingPrice());
            ps.setString(6, item.getDescription());
            ps.setString(7, item.getImageUrl());

            int row = ps.executeUpdate();
            return row > 0;
        }
    }

    public Item findById(Connection conn, String id) throws SQLException {
        String sql = "SELECT * FROM items WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return AuctionMapper.extractItemFromDB(rs);
                }
            }
        }
        return null;
    }

}
