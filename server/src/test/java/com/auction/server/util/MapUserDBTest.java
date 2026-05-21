package com.auction.server.util;

import com.auction.share.models.user.Admin;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapUserDBTest {

    @Test
    void mapUserShouldMapSellerCorrectly() throws SQLException {
        ResultSet rs = resultSetOf(Map.of(
                "id", "user-1",
                "username", "seller01",
                "password", "hashed",
                "fullname", "Seller Name",
                "phoneNumber", "0123456789",
                "email", "seller@mail.com",
                "role", "SELLER",
                "balance", 1250.5,
                "address", "HCM City",
                "access_level", 0
        ));

        User user = MapUserDB.mapUser(rs);

        assertInstanceOf(Seller.class, user);
        assertEquals("user-1", user.getId());
        assertEquals("seller01", user.getUsername());
        assertEquals(1250.5, ((Seller) user).getBalance());
    }

    @Test
    void mapUserShouldMapBidderCorrectly() throws SQLException {
        ResultSet rs = resultSetOf(Map.of(
                "id", "user-2",
                "username", "bidder01",
                "password", "hashed2",
                "fullname", "Bidder Name",
                "phoneNumber", "0999999999",
                "email", "bidder@mail.com",
                "role", "BIDDER",
                "balance", 500.0,
                "address", "Da Nang",
                "access_level", 0
        ));

        User user = MapUserDB.mapUser(rs);

        assertInstanceOf(Bidder.class, user);
        assertEquals("user-2", user.getId());
        assertEquals("bidder01", user.getUsername());
        assertEquals(500.0, ((Bidder) user).getBalance());
    }

    @Test
    void mapUserShouldMapAdminCorrectly() throws SQLException {
        ResultSet rs = resultSetOf(Map.of(
                "id", "admin-1",
                "username", "admin01",
                "password", "hashed3",
                "fullname", "Admin Name",
                "phoneNumber", "",
                "email", "",
                "role", "ADMIN",
                "balance", 0.0,
                "address", "",
                "access_level", 2
        ));

        User user = MapUserDB.mapUser(rs);

        assertInstanceOf(Admin.class, user);
        assertEquals("admin-1", user.getId());
        assertEquals(2, ((Admin) user).getAccessLevel());
    }

    @Test
    void mapUserShouldThrowForUnsupportedRole() {
        ResultSet rs = resultSetOf(Map.of(
                "id", "x",
                "username", "u",
                "password", "p",
                "fullname", "n",
                "phoneNumber", "",
                "email", "",
                "role", "UNKNOWN",
                "balance", 0.0,
                "address", "",
                "access_level", 0
        ));

        SQLException ex = assertThrows(SQLException.class, () -> MapUserDB.mapUser(rs));
        assertTrue(ex.getMessage().contains("Unsupported role"));
    }

    @Test
    void mapUserShouldThrowWhenRoleIsNull() {
        ResultSet rs = resultSetOf(Map.of(
                "id", "x",
                "username", "u",
                "password", "p",
                "fullname", "n",
                "phoneNumber", "",
                "email", "",
                "balance", 0.0,
                "address", "",
                "access_level", 0
        ));

        assertThrows(NullPointerException.class, () -> MapUserDB.mapUser(rs));
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
                    if ("getInt".equals(name)) {
                        Object value = safeValues.get((String) args[0]);
                        return value == null ? 0 : ((Number) value).intValue();
                    }
                    throw new UnsupportedOperationException("Method not supported in test ResultSet: " + name);
                }
        );
    }
}
