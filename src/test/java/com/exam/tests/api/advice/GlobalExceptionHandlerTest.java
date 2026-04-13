package com.exam.tests.api.advice;

import com.exam.api.advice.GlobalExceptionHandler;
import com.exam.api.common.ApiResponse;
import com.exam.exception.AuthenticationException;
import com.exam.exception.BusinessException;
import com.exam.exception.DatabaseException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleAuthenticationShouldKeepBusinessMessage() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleAuthentication(new AuthenticationException("未登录"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("未登录", response.getBody().getMessage());
    }

    @Test
    void handleBusinessShouldKeepBusinessMessage() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleBusiness(new BusinessException("参数错误"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("参数错误", response.getBody().getMessage());
    }

    @Test
    void handleDatabaseShouldReturnGenericMessage() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleDatabase(new DatabaseException("SQL syntax error"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("系统繁忙，请稍后重试", response.getBody().getMessage());
    }

    @Test
    void handleOtherShouldReturnGenericMessage() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleOther(new RuntimeException("NullPointerException"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("系统开小差了，请稍后重试", response.getBody().getMessage());
    }
}
