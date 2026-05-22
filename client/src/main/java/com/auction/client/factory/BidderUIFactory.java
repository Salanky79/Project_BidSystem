package com.auction.client.factory;

/**
 * Concrete Factory khởi tạo giao diện cho người đấu giá (Bidder).
 */
public class BidderUIFactory implements RoleUIFactory {
    @Override
    public DashboardProduct createDashboard() {
        return new BidderDashboard();
    }
}
