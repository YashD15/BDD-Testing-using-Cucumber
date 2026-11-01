# Day 11 — Input Validation and Monetary Precision

## Objective

Strengthen validation for wallet transactions and transfers to prevent
invalid monetary amounts from reaching the business logic.

## Changes Implemented

- Added `@Digits` validation to `CreateTransactionRequestDto`.
- Added `@Digits` validation to `TransferRequestDto`.
- Limited monetary amounts to two decimal places.
- Added a controller test for transaction amount precision.
- Added a controller test for transfer amount precision.
- Preserved existing business-conflict handling.

## Monetary Validation Rules

| Amount | Expected Result |
|---|---|
| 10 | Valid |
| 10.5 | Valid |
| 10.55 | Valid |
| 10.555 | Invalid |
| 0 | Invalid |
| -10 | Invalid |

Amounts must be at least 0.01 and have no more than two decimal places.

## Error Handling

- Invalid request validation: `400 Bad Request`
- Insufficient balance: `409 Conflict`
- Missing wallet: `404 Not Found`

## Verification

Run the tests:

    .\gradlew.bat clean test

Build the project:

    .\gradlew.bat build

## Result

Transaction and transfer request DTOs enforce monetary precision, and
controller tests verify that amounts with excessive decimal places
are rejected.