package com.example.wallet.exception;

public class BusinessConflictException extends RuntimeException {

  public BusinessConflictException(String message) {
    super(message);
  }
}