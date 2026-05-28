package com.auction.server.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/** Lớp cấu hình kết nối cơ sở dữ liệu (Database), sử dụng HikariCP để quản lý connection pool. */
public class DatabaseConnection {
  private static final HikariConfig config = new HikariConfig();
  private static HikariDataSource ds;

  private DatabaseConnection() {}

  private static void initDataSource() {
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

    config.setMaximumPoolSize(10); // giới hạn tối đa 10 kết nối cùng lúc đến DB
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

    ds = new HikariDataSource(config);
  }

  // Use only for testing
  private static Connection testConnection;
  public static void setTestConnection(Connection conn) {
    testConnection = conn;
  }

  public static synchronized Connection getConnection() throws SQLException {
    if (testConnection != null) {
      return testConnection;
    }
    if (ds == null) {
      initDataSource();
    }
    return ds.getConnection();
  }

  public static synchronized void shutdown() {
    if (ds != null && !ds.isClosed()) {
      ds.close();
    }
  }
}
