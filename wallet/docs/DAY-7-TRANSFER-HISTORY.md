# Day 7 — Transfer History

## Goal

Allow users to retrieve incoming and outgoing wallet transfers.

## Implemented

- Created `TransferHistoryResponseDto`.
- Added a repository query for transfer history by wallet ID.
- Added `getTransferHistory` to `TransferService`.
- Mapped transfer records to history DTOs.
- Added `GET /api/transfers?userId={userId}`.
- Returned transfer history in newest-first order.
- Added controller tests for successful history, empty history,
  and missing wallets.

## API Example

    GET /api/transfers?userId=101

The endpoint returns a JSON array containing incoming and
outgoing transfers for the user's active wallet.

## Verification

    .\gradlew.bat clean test
    .\gradlew.bat build

## Key Learnings

- Repository queries can retrieve both incoming and outgoing records.
- DTOs keep API responses separate from persistence entities.
- Controller tests verify HTTP status codes and JSON responses.
