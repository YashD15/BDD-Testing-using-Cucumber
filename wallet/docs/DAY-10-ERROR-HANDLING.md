# Day 10 — Standardized API Error Handling

## Objective

Provide a consistent JSON error response across the Digital Wallet API
and return appropriate HTTP status codes for invalid input, missing
resources, business conflicts, and unexpected server errors.

## Implementation

- Created `ApiErrorResponse` as the common error-response DTO.
- Updated `GlobalExceptionHandler` to return a consistent response.
- Added `BusinessConflictException` to distinguish business conflicts
  from invalid input.
- Added handling for invalid request bodies and query-parameter types.
- Added tests for standard error fields and HTTP status codes.

## Standard Error Response

Example:

    {
      "timestamp": "2026-10-29T10:30:00",
      "status": 400,
      "error": "Bad Request",
      "message": "Invalid page number",
      "path": "/api/transfers"
    }

## HTTP Status Codes

| Status | Meaning | Example |
|---|---|---|
| 400 Bad Request | Invalid input | Invalid status, date, page, or size |
| 404 Not Found | Resource not found | Wallet does not exist |
| 409 Conflict | Business conflict | Duplicate wallet or insufficient funds |
| 500 Internal Server Error | Unexpected server failure | Unhandled internal exception |

## Verification

Run the test suite:

    .\gradlew.bat clean test

Build the project:

    .\gradlew.bat build

## Result

The API uses a consistent error-response structure, and tests verify
the main HTTP status codes and error fields.