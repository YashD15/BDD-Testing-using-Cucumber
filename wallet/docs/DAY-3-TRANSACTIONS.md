# Day 3 — Wallet Transactions

## Goal

Implement credit and debit operations, maintain accurate wallet balances, and provide transaction history for each wallet.

## Tasks Completed

- Created the `TransactionType` enum with `CREDIT` and `DEBIT`.
- Created the `WalletTransaction` JPA entity.
- Created `WalletTransactionRepository`.
- Implemented transaction request and response DTOs.
- Created `WalletTransactionService` and `WalletTransactionServiceImpl`.
- Created `WalletTransactionController`.
- Added validation for transaction amounts and types.
- Implemented insufficient-balance checks.
- Added pessimistic locking to protect wallet balance updates.
- Implemented transaction history retrieval.
- Added service unit tests and controller integration tests.

## Transaction Model

The `WalletTransaction` entity stores:

- `id` — unique transaction identifier.
- `walletId` — wallet associated with the transaction.
- `transactionType` — `CREDIT` or `DEBIT`.
- `amount` — transaction amount.
- `balanceAfter` — wallet balance after the transaction.
- `description` — optional transaction description.
- `createdAt` — transaction timestamp.

Transactions serve as an audit history and are not soft-deleted.

## API Functionality

### Create a Transaction

`POST /api/transactions`

Supports crediting or debiting a wallet.

### Retrieve Transaction History

`GET /api/transactions?userId=1`

Returns the transaction history associated with the specified user's wallet, ordered newest first.

## Business Rules

- Transaction amounts must be greater than zero.
- A wallet must exist before a transaction can be created.
- Credit transactions increase the wallet balance.
- Debit transactions decrease the wallet balance.
- Withdrawals exceeding the available balance are rejected.
- Failed withdrawals must not create transaction records.
- Wallet balance updates and transaction persistence are handled within the service's transactional operation.

Pessimistic locking is used when retrieving the wallet for a transaction to help prevent concurrent updates from spending the same balance.

## Testing

Service unit tests cover:

- Successful credit transactions.
- Successful debit transactions.
- Insufficient-balance rejection.
- Transaction history retrieval.
- Missing-wallet handling.

Controller integration tests cover successful transaction creation, invalid amounts, history retrieval, and missing wallets.

Run the automated tests using:

```powershell
.\gradlew.bat test
```

## Key Learnings

- Modeling transaction types with Java enums.
- Using `BigDecimal` for monetary values.
- Applying validation at the request and service layers.
- Managing entity updates with JPA dirty checking.
- Understanding Spring transaction management and database locking.
- Maintaining transaction audit records.
- Testing service logic and REST controllers.
