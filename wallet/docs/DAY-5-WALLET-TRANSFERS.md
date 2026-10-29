# Day 5 — Wallet-to-Wallet Transfers

## Goal

Implement transfers between two users' wallets while maintaining balance consistency, transaction history, and business-rule validation.

## Tasks Completed

- Created `TransferRequestDto` and `TransferResponseDto`.
- Implemented the `TransferService` interface and `TransferServiceImpl`.
- Added the `POST /api/transfers` REST endpoint.
- Implemented sender debit and receiver credit operations.
- Added validation for same-user transfers, insufficient funds, missing wallets, and currency mismatches.
- Used transactional processing and pessimistic wallet locking.
- Recorded transfers as debit and credit entries in the existing transaction history.
- Added service unit tests and controller tests.
- Added Cucumber scenarios for successful and rejected transfers.
- Verified the complete test suite and build.

## Transfer Behaviour

A successful transfer:

1. Validates the sender, receiver, and amount.
2. Locks both wallets while processing the transfer.
3. Checks currency compatibility and available funds.
4. Debits the sender and credits the receiver.
5. Records both transactions.
6. Returns the transfer result and updated balances.

The transfer uses Spring's `@Transactional` so database changes roll back together if an operation fails.

## API Endpoint

`POST /api/transfers`

Example request:

```json
{
  "senderUserId": 1,
  "receiverUserId": 2,
  "amount": 200.00,
  "description": "Payment to another user"
}
```

A successful response includes the transfer identifier, sender and receiver IDs, amount, currency, resulting balances, timestamp, and status.

## Testing

Tests cover:

- Successful transfers.
- Insufficient sender balance.
- Transfers to the same user.
- Missing receiver wallets.
- Request validation.
- Creation of debit and credit transaction records.
- Preservation of wallet balances when a transfer is rejected.

Run all tests:

```powershell
.\gradlew.bat clean test
```

Run the full build:

```powershell
.\gradlew.bat build
```

Both commands completed successfully.

## Key Learnings

- Implementing multi-wallet business operations.
- Using `@Transactional` for atomic database updates.
- Using pessimistic locking to help protect against concurrent balance updates.
- Recording transfers through debit and credit transaction entries.
- Testing business logic separately from REST controllers.
- Using Cucumber to describe transfer behaviour.
