package com.exam.util;

import com.exam.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {
    private static HikariDataSource dataSource;

    static {
        try {
            Properties props = new Properties();
            InputStream is = DBUtil.class.getClassLoader().getResourceAsStream("db.properties");
            if (is == null) {
                throw new DatabaseException("无法找到数据库配置文件 db.properties");
            }
            props.load(is);
            is.close();

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(resolveConfig("DB_URL", props.getProperty("db.url")));
            config.setUsername(resolveConfig("DB_USERNAME", props.getProperty("db.username")));
            config.setPassword(resolveConfig("DB_PASSWORD", props.getProperty("db.password")));
            config.setDriverClassName(resolveConfig("DB_DRIVER", props.getProperty("db.driver")));

            config.setMaximumPoolSize(20);
            config.setMinimumIdle(5);
            config.setIdleTimeout(300000);
            config.setConnectionTimeout(30000);
            config.setMaxLifetime(1800000);
            config.setPoolName("ExamSystemPool");

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");

            dataSource = new HikariDataSource(config);
        } catch (IOException e) {
            throw new DatabaseException("加载数据库配置文件失败", e);
        }
    }

    public static Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DatabaseException("获取数据库连接失败", e);
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new DatabaseException("关闭数据库连接失败", e);
            }
        }
    }

    public static void close(AutoCloseable autoCloseable) {
        if (autoCloseable != null) {
            try {
                autoCloseable.close();
            } catch (Exception e) {
                throw new DatabaseException("关闭资源失败", e);
            }
        }
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public static String getPoolStats() {
        if (dataSource != null) {
            return String.format("连接池状态: 总连接数=%d, 活跃连接=%d, 空闲连接=%d, 等待线程=%d",
                    dataSource.getHikariPoolMXBean().getTotalConnections(),
                    dataSource.getHikariPoolMXBean().getActiveConnections(),
                    dataSource.getHikariPoolMXBean().getIdleConnections(),
                    dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        }
        return "连接池未初始化";
    }

    private static String resolveConfig(String envKey, String fallback) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }
        return fallback;
    }
}
