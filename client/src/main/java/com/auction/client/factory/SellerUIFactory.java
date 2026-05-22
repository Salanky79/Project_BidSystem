package com.auction.client.factory;

/**
 * Concrete Factory khởi tạo giao diện cho người bán (Seller).
 */
public class SellerUIFactory implements RoleUIFactory {
    @Override
    public DashboardProduct createDashboard() {
        return new SellerDashboardProduct();
    }
}
