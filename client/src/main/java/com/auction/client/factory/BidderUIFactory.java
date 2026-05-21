package com.auction.client.factory;

/**
 * Factory tạo dashboard cho bidder.
 */
public class BidderUIFactory implements RoleUIFactory {
    @Override
    public DashboardProduct createDashboard() {
        return new BidderDashboard();
    }
}
