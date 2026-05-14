package com.auction.server.dao;

import com.auction.server.util.DatabaseConnection;
import com.auction.server.util.MapAuctionDB;
import com.auction.share.models.item.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class ItemDAO {
    
    public boolean saveItem(Item item) throws SQLException {
        String sql = "INSERT INTO items (id, seller_id, name, category, starting_price, description, condition, era, material, brand, warranty_period, artist, year, caratWeight, location, area, fuelType) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, item.getId());
            ps.setString(2, item.getSellerId());
            ps.setString(3, item.getName());
            ps.setString(4, item.getCategory().name());
            ps.setDouble(5, item.getStartingPrice());
            ps.setString(6, item.getDescription());
            
            // Default nulls for subclass fields
            ps.setNull(7, Types.VARCHAR); // condition
            ps.setNull(8, Types.VARCHAR); // era
            ps.setNull(9, Types.VARCHAR); // material
            ps.setNull(10, Types.VARCHAR); // brand
            ps.setNull(11, Types.INTEGER); // warranty_period
            ps.setNull(12, Types.VARCHAR); // artist
            ps.setNull(13, Types.INTEGER); // year
            ps.setNull(14, Types.DECIMAL); // caratWeight
            ps.setNull(15, Types.VARCHAR); // location
            ps.setNull(16, Types.DOUBLE); // area
            ps.setNull(17, Types.VARCHAR); // fuelType
            
            // Bind specific fields based on subclass
            if (item instanceof Antique antique) {
                ps.setString(8, antique.getEra());
                ps.setString(9, antique.getMaterial());
            } else if (item instanceof Art art) {
                ps.setString(12, art.getArtist());
                ps.setInt(13, art.getYear());
            } else if (item instanceof Electronic electronic) {
                ps.setString(10, electronic.getBrand());
                ps.setInt(11, electronic.getWarrantyMonths());
            } else if (item instanceof Jewelry jewelry) {
                ps.setString(9, jewelry.getMaterial());
                ps.setDouble(14, jewelry.getCaratWeight());
            } else if (item instanceof RealEstate realEstate) {
                ps.setString(15, realEstate.getLocation());
                ps.setDouble(16, realEstate.getAreaSquareMeter());
            } else if (item instanceof Vehicle vehicle) {
                ps.setString(17, vehicle.getFuelType());
            }
            
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
