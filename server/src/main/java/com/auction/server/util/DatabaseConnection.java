package com.auction.server.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

/** Lớp cấu hình kết nối cơ sở dữ liệu (Database), sử dụng HikariCP để quản lý connection pool. */
public class DatabaseConnection {
  private DatabaseConnection() {}

  public static HikariDataSource createDataSource() {
    HikariConfig config = new HikariConfig();
    String dbUrl = AppConfig.get("DB_URL");
    String dbUser = AppConfig.get("DB_USER");
    String dbPass = AppConfig.get("DB_PASS");

    if (dbUrl == null || dbUrl.isBlank()) {
      throw new IllegalStateException("DB_URL is missing.");
    }
    if (dbUser == null || dbUser.isBlank()) {
      throw new IllegalStateException("DB_USER is missing.");
    }
    if (dbPass == null || dbPass.isBlank()) {
      throw new IllegalStateException("DB_PASS is missing.");
    }

    config.setJdbcUrl(dbUrl);
    config.setUsername(dbUser);
    config.setPassword(dbPass);

    int poolSize = Integer.parseInt(AppConfig.getOrDefault("DB_POOL_SIZE", "20"));
    config.setMaximumPoolSize(poolSize);
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

    return new HikariDataSource(config);
  }
}
