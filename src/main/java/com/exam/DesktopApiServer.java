package com.exam;

import com.exam.api.ApiApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Electron 桌面端使用的无界面 Spring Boot 后端入口。
 */
public class DesktopApiServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopApiServer.class);

    public static void main(String[] args) {
        LOGGER.info("Starting Spring Boot API for Electron desktop.");
        ApiApplication.main(args);
    }
}
