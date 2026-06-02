package com.auction.client.factory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleUIFactoryTest {

    @Test
    void testBidderUIFactory_createsBidderDashboard() {
        RoleUIFactory factory = new BidderUIFactory();
        DashboardProduct dashboard = factory.createDashboard();
        assertTrue(dashboard instanceof BidderDashboard, "BidderUIFactory should create a BidderDashboard");
    }

    @Test
    void testSellerUIFactory_createsSellerDashboard() {
        RoleUIFactory factory = new SellerUIFactory();
        DashboardProduct dashboard = factory.createDashboard();
        assertTrue(dashboard instanceof SellerDashboardProduct, "SellerUIFactory should create a SellerDashboardProduct");
    }
}
