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
        return Dotenv.load();
    }

    private DatabaseConnection(){}

    static {
        config.setJdbcUrl(dotenv.get("DB_URL"));
        config.setUsername(dotenv.get("DB_USER"));
        config.setPassword(dotenv.get("DB_PASS"));

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
