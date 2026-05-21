package com.auction.client.factory;

/**
 * Factory tạo dashboard cho seller.
 */
public class SellerUIFactory implements RoleUIFactory {
    @Override
    public DashboardProduct createDashboard() {
        return new SellerDashboardProduct();
    }
}
