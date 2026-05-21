package com.auction.server.util;

import io.github.cdimascio.dotenv.Dotenv;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;
    public static Dotenv dotenv = loadDotenv();

    private static Dotenv loadDotenv() {
        return Dotenv.configure().ignoreIfMissing().load();
    }

    private static String getConfigValue(String key) {
        String systemValue = System.getenv(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }
        return dotenv.get(key);
    }

    private DatabaseConnection(){}

    static {
        String dbUrl = getConfigValue("DB_URL");
        String dbUser = getConfigValue("DB_USER");
        String dbPass = getConfigValue("DB_PASS");

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

        config.setMaximumPoolSize(10); // Tối đa 10 kết nối cùng lúc
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException{
        return ds.getConnection();
    }

    public static void shutdown(){
        if(ds != null && !ds.isClosed()){
            ds.close();
        }
    }
}
