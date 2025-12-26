package com.exam.util;

import com.exam.exception.DatabaseException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具类
 * 使用单例模式和连接池管理数据库连接
 */
public class DBUtil {
    private static String url;
    private static String username;
    private static String password;
    private static String driver;

    // 静态代码块，加载数据库配置
    static {
        try {
            Properties props = new Properties();
            InputStream is = DBUtil.class.getClassLoader().getResourceAsStream("db.properties");
            if (is == null) {
                throw new DatabaseException("无法找到数据库配置文件 db.properties");
            }
            props.load(is);
            
            url = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");
            driver = props.getProperty("db.driver");
            
            // 加载驱动
            Class.forName(driver);
        } catch (IOException e) {
            throw new DatabaseException("加载数据库配置文件失败", e);
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("加载数据库驱动失败", e);
        }
    }

    /**
     * 获取数据库连接
     * @return 数据库连接
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new DatabaseException("获取数据库连接失败", e);
        }
    }

    /**
     * 关闭数据库连接
     * @param conn 数据库连接
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
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
}
