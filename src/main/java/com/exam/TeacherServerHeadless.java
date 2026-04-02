package com.exam;

import com.exam.api.ApiApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Electron 桌面端使用的无界面后端入口。
 * 保留 TeacherServerHeadless 这个类名，是为了兼容现有 Maven 打包配置。
 */
public class TeacherServerHeadless {
    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherServerHeadless.class);

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        LOGGER.info("Starting Spring Boot API in headless mode for Electron desktop.");
        ApiApplication.main(args);
    }
}
