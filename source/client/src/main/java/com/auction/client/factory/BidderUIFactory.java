package com.auction.client.factory;

public class BidderUIFactory implements RoleUIFactory {
  @Override
  public DashboardProduct createDashboard() {
    return new BidderDashboard();
  }
}
