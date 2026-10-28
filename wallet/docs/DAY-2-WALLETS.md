# Day 2 — Wallet Management

## Goal

Implement wallet creation and retrieval functionality using a layered Spring Boot architecture with DTOs, Lombok, validation, and centralized exception handling.

## Tasks Completed

- Created the `Wallet` JPA entity.
- Created `WalletRepository` for database operations.
- Implemented request and response DTOs.
- Created the `WalletService` interface and `WalletServiceImpl`.
- Created `WalletController` REST endpoints.
- Added request validation and global exception handling.
- Implemented duplicate-wallet prevention.
- Added unit tests for wallet service functionality.

## Wallet Model

The `Wallet` entity contains:

- `id` — unique wallet identifier.
- `userId` — identifier of the existing user who owns the wallet.
- `balance` — current wallet balance, initialized to zero.
- `currency` — wallet currency, currently restricted to INR.
- `createdAt` — wallet creation timestamp.
- `updatedAt` — last update timestamp.
- `isDeleted` — soft-delete status.

Each user can have only one active wallet.

## API Functionality

### Create a Wallet

`POST /api/wallets`

Accepts a user ID and currency. A newly created wallet starts with a zero balance.

### Retrieve a Wallet

`GET /api/wallets?userId=1`

Returns the active wallet belonging to the specified user.

## Validation and Exception Handling

- User ID must be provided and greater than zero.
- Currency is required and must be `INR`.
- Duplicate active wallets are rejected.
- Missing wallets return `404 Not Found`.
- Invalid request data returns `400 Bad Request`.
- Duplicate-wallet errors return `409 Conflict`.

A global exception handler centralizes API error responses.

## Testing

Created unit tests covering:

- Successful wallet creation.
- Duplicate wallet rejection.
- Retrieving an existing wallet.
- Handling a missing wallet.

The wallet API was also tested manually to verify creation, duplicate rejection, and retrieval.

Run the automated tests using:

```powershell
.\gradlew.bat test
```

## Key Learnings

- Using JPA entities and Spring Data repositories.
- Separating request and response DTOs from database entities.
- Implementing service and controller layers.
- Applying validation with Jakarta Bean Validation.
- Using `ResponseEntity` and centralized exception handling.
- Understanding soft-delete flags and database constraints.
- Writing unit tests using mocks.
