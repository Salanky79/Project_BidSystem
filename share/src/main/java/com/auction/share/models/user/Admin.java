package com.auction.share.models.user;

import com.auction.share.enums.Role;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Admin extends User {
    private int accessLevel;

    public Admin(String username, String password, String fullName, int accessLevel) {
        super(username, password, fullName);
        this.accessLevel = accessLevel;
        this.setRole(Role.ADMIN);
    }

    public int getAccessLevel(){
        return accessLevel;
    }

    // Trong Admin
    @Override
    public void fillPreparedStatement(PreparedStatement ps) throws SQLException {
        ps.setNull(5, java.sql.Types.VARCHAR);
        ps.setNull(6, java.sql.Types.VARCHAR);
        ps.setDouble(8, 0.0);
        ps.setNull(9, java.sql.Types.VARCHAR);
        ps.setInt(10, this.accessLevel);
    }
}

