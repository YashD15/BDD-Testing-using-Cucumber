# Day 6 — Transfer Reliability and Data Integrity

## Goal

Improve wallet transfer traceability and verify that transfers
are processed atomically.

## Implemented

- Added the `WalletTransfer` entity.
- Added the `WalletTransferRepository`.
- Created a dedicated database record for each successful transfer.
- Updated `TransferServiceImpl` to save the transfer record and
  the sender's debit and receiver's credit transactions.
- Updated the transfer response to return the dedicated transfer ID.
- Standardized successful transfer status as `COMPLETED`.
- Added tests for successful transfers, insufficient funds,
  missing wallets, same-user transfers, and currency mismatches.
- Added a database-backed rollback test.

## Data Integrity

A successful transfer updates both wallet balances and records
the transfer, debit, and credit within one transaction.

If a database write fails, the transaction should roll back
all associated changes.

## Database Records

### wallet_transfers

Stores the overall transfer:

- Transfer ID
- Sender wallet ID
- Receiver wallet ID
- Amount
- Currency
- Status
- Description
- Creation timestamp

### wallet_transactions

Stores the individual financial events:

- Sender debit
- Receiver credit
- Balance after each transaction
- Transaction timestamp

The transaction descriptions include the transfer ID for
traceability.

## Verification Commands

Run the complete test suite:

    .\gradlew.bat clean test

Build the project:

    .\gradlew.bat build

Run the rollback integration test independently:

    .\gradlew.bat test --tests "com.example.wallet.service.impl.TransferRollbackIntegrationTest"

## Key Learnings

- Why financial transfers require atomic database transactions.
- How a dedicated transfer record improves traceability.
- Why a unit test alone cannot prove database rollback.
- How integration tests verify persistence behavior.
