package com.example.wallet.exception;

import com.example.wallet.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler exceptionHandler;
  private HttpServletRequest request;

  @BeforeEach
  void setUp() {
    exceptionHandler = new GlobalExceptionHandler();
    request = mock(HttpServletRequest.class);

    when(request.getRequestURI()).thenReturn("/api/transfers");
  }

  @Test
  void illegalArgumentShouldReturnBadRequest() {
    ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleIllegalArgument(
        new IllegalArgumentException("Invalid page number"),
        request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertEquals("Bad Request", response.getBody().getError());
    assertEquals("Invalid page number", response.getBody().getMessage());
    assertEquals("/api/transfers", response.getBody().getPath());
    assertNotNull(response.getBody().getTimestamp());
  }

  @Test
  void businessConflictShouldReturnConflict() {
    ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleBusinessConflict(
        new BusinessConflictException("Insufficient wallet balance"),
        request);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(409, response.getBody().getStatus());
    assertEquals("Conflict", response.getBody().getError());
    assertEquals("Insufficient wallet balance", response.getBody().getMessage());
  }

  @Test
  void walletNotFoundShouldReturnNotFound() {
    ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleWalletNotFound(
        new WalletNotFoundException("Wallet not found"),
        request);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(404, response.getBody().getStatus());
    assertEquals("Not Found", response.getBody().getError());
    assertEquals("Wallet not found", response.getBody().getMessage());
  }

  @Test
  void unexpectedExceptionShouldHideInternalDetails() {
    ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleUnexpectedException(
        new RuntimeException("Sensitive internal detail"),
        request);

    assertEquals(
        HttpStatus.INTERNAL_SERVER_ERROR,
        response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(500, response.getBody().getStatus());
    assertEquals(
        "An unexpected error occurred",
        response.getBody().getMessage());
    assertNotEquals(
        "Sensitive internal detail",
        response.getBody().getMessage());
  }
}