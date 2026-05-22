package com.auction.client.factory;

/**
 * Interface định nghĩa Abstract Factory để tạo các màn hình giao diện tùy theo vai trò.
 */
public interface RoleUIFactory {
    DashboardProduct createDashboard();
}
