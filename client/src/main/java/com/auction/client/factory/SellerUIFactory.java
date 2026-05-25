package com.auction.client.factory;

public class SellerUIFactory implements RoleUIFactory {
  @Override
  public DashboardProduct createDashboard() {
    return new SellerDashboardProduct();
  }
}
