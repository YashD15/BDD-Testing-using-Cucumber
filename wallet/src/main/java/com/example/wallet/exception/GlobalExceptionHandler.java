package com.example.wallet.exception;

import com.example.wallet.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
                        IllegalArgumentException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                ex.getMessage(),
                                request);
        }

        @ExceptionHandler(BusinessConflictException.class)
        public ResponseEntity<ApiErrorResponse> handleBusinessConflict(
                        BusinessConflictException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.CONFLICT,
                                ex.getMessage(),
                                request);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidationErrors(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                String message = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .findFirst()
                                .map(error -> error.getDefaultMessage())
                                .orElse("Invalid request");

                return buildResponse(HttpStatus.BAD_REQUEST, message, request);
        }

        @ExceptionHandler(WalletNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleWalletNotFound(
                        WalletNotFoundException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.NOT_FOUND,
                                ex.getMessage(),
                                request);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
                        MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "Invalid value for parameter: " + ex.getName(),
                                request);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiErrorResponse> handleUnreadableRequest(
                        HttpMessageNotReadableException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "Request body is missing or contains invalid data",
                                request);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
                        Exception ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "An unexpected error occurred",
                                request);
        }

        private ResponseEntity<ApiErrorResponse> buildResponse(
                        HttpStatus status,
                        String message,
                        HttpServletRequest request) {

                ApiErrorResponse response = ApiErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(status.value())
                                .error(status.getReasonPhrase())
                                .message(message != null ? message : status.getReasonPhrase())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(status).body(response);
        }
}