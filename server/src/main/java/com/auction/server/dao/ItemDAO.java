package com.auction.server.dao;

import com.auction.server.util.DatabaseConnection;
import com.auction.server.util.MapAuctionDB;
import com.auction.share.models.item.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemDAO {
    
    public boolean saveItem(Item item) throws SQLException {
        String sql = "INSERT INTO items (id, seller_id, name, category, starting_price, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, item.getId());
            ps.setString(2, item.getSellerId());
            ps.setString(3, item.getName());
            ps.setString(4, item.getCategory().name());
            ps.setDouble(5, item.getStartingPrice());
            ps.setString(6, item.getDescription());

            int row = ps.executeUpdate();
            return row > 0;
        }
    }

    public Item findById(String id) throws SQLException {
        String sql = "SELECT * FROM items WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return MapAuctionDB.mapItem(rs);
                }
            }
        }
        return null;
    }
}
