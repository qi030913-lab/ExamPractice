package com.exam.util;

import com.exam.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具类
 * 使用HikariCP连接池管理数据库连接，提升性能
 */
public class DBUtil {
    private static HikariDataSource dataSource;

    // 静态代码块，初始化连接池
    static {
        try {
            Properties props = new Properties();
            InputStream is = DBUtil.class.getClassLoader().getResourceAsStream("db.properties");
            if (is == null) {
                throw new DatabaseException("无法找到数据库配置文件 db.properties");
            }
            props.load(is);
            is.close();
            
            // 配置HikariCP连接池
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setDriverClassName(props.getProperty("db.driver"));
            
            // 连接池性能配置
            config.setMaximumPoolSize(20);           // 最大连接数
            config.setMinimumIdle(5);                // 最小空闲连接数
            config.setIdleTimeout(300000);           // 空闲连接超时时间(5分钟)
            config.setConnectionTimeout(30000);      // 连接超时时间(30秒)
            config.setMaxLifetime(1800000);          // 连接最大生存时间(30分钟)
            config.setPoolName("ExamSystemPool");    // 连接池名称
            
            // 性能优化配置
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

    /**
     * 获取数据库连接（从连接池获取）
     * @return 数据库连接
     */
    public static Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DatabaseException("获取数据库连接失败", e);
        }
    }

    /**
     * 关闭数据库连接（归还到连接池）
     * @param conn 数据库连接
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close(); // HikariCP会自动归还到连接池
            } catch (SQLException e) {
                throw new DatabaseException("关闭数据库连接失败", e);
            }
        }
    }

    /**
     * 关闭资源（支持自动关闭的资源）
     * @param autoCloseable 可自动关闭的资源
     */
    public static void close(AutoCloseable autoCloseable) {
        if (autoCloseable != null) {
            try {
                autoCloseable.close();
            } catch (Exception e) {
                throw new DatabaseException("关闭资源失败", e);
            }
        }
    }
    
    /**
     * 关闭连接池（应用程序关闭时调用）
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    
    /**
     * 获取连接池状态信息（用于调试）
     */
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
}
