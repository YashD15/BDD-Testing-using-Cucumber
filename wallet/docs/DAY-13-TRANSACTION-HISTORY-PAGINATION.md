# Day 13 — Transaction History Pagination

## Objective

Implement pagination for wallet transaction history so clients can retrieve
transactions in manageable pages instead of loading the entire history.

## Features Implemented

- Added pagination to `GET /api/transactions`.
- Added `page` and `size` request parameters.
- Default page number: `0`.
- Default page size: `10`.
- Maximum page size: `100`.
- Added validation for page and size.
- Transactions are returned newest first.
- Used transaction ID as a secondary sort key for stable ordering.
- Preserved existing wallet lookup and error-handling behavior.
- Returned Spring Data's `Page` response with pagination metadata.
- Explicitly named request parameters to avoid reliance on Java
  compiler parameter-name metadata.

## API Endpoint

`GET /api/transactions`

### Query Parameters

| Parameter | Required | Default | Description |
|---|---|---|---|
| `userId` | Yes | — | User whose wallet history is requested |
| `page` | No | `0` | Zero-based page index |
| `size` | No | `10` | Number of records per page; maximum 100 |

### Example Request

`GET /api/transactions?userId=1&page=0&size=10`

### Response

The response is a paginated object containing:

- `content`: transaction records for the current page
- `totalElements`: total matching transaction records
- `totalPages`: total number of pages
- `number`: current zero-based page index
- `size`: requested page size

Other metadata fields may also be included by Spring Data.

## Sorting

Transactions are sorted by:

1. `createdAt` descending
2. `id` descending

The transaction ID provides a deterministic secondary sort when multiple
transactions have the same creation timestamp.

## Validation and Error Handling

| Condition | Expected HTTP Status |
|---|---|
| Valid request for an existing wallet | `200 OK` |
| Missing wallet | `404 Not Found` |
| Negative page number | `400 Bad Request` |
| Page size below 1 | `400 Bad Request` |
| Page size above 100 | `400 Bad Request` |
| Invalid request parameter | `400 Bad Request` |

Errors use the standardized API error response format.

## Implementation Layers Updated

- `WalletTransactionRepository`
- `WalletTransactionService`
- `WalletTransactionServiceImpl`
- `WalletTransactionController`
- Transaction service and controller tests
- Cucumber step definitions and test helpers where required

## Verification

- [ ] `.\gradlew.bat clean test` passes.
- [ ] `.\gradlew.bat build` passes.
- [ ] Application starts successfully using `.\gradlew.bat bootRun`.
- [ ] `GET /api/health` returns `200 OK`.
- [ ] `GET /api/health/database` returns `200 OK`.
- [ ] Transaction history returns paginated data.
- [ ] Invalid page and size values return `400 Bad Request`.
- [ ] Missing wallet returns `404 Not Found`.
- [ ] Transfer history pagination smoke tests pass.
- [ ] Newest-first ordering is verified.

## Outcome

Wallet transaction history supports pagination with stable newest-first
ordering, validated page parameters, and pagination metadata. Existing
transaction creation and standardized error handling remain supported.