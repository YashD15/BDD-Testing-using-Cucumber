# Day 14 - Wallet Soft Deletion & Lifecycle Safety

## Goal

Implement wallet soft deletion while preserving financial audit history and preventing deleted wallets from participating in new operations.

## Features Implemented

### 1. Wallet Soft Deletion
- Added `deleteWallet(Long userId)` to WalletService.
- Implemented soft deletion in WalletServiceImpl.
- Sets `isDeleted = true` instead of physically deleting the wallet.
- Updates the wallet's `updatedAt` timestamp.
- Returns HTTP 204 No Content after successful deletion.

### 2. Deleted Wallet Access Protection
- Deleted wallets are excluded from normal wallet retrieval.
- Attempting to retrieve a deleted wallet returns 404 Not Found.
- Attempting to delete an already-deleted wallet returns 404 Not Found.

### 3. Transaction Lifecycle Safety
- New deposits and withdrawals are rejected for deleted wallets.
- Existing transaction history remains stored.
- Deleted wallets cannot be accessed through normal transaction-history retrieval.

### 4. Transfer Lifecycle Safety
- Deleted wallets cannot initiate transfers.
- Deleted wallets cannot receive transfers.
- Failed transfers do not modify wallet balances.
- Failed transfers do not create transaction records.
- Existing transfer records remain preserved.

### 5. Test Coverage
- Added WalletServiceImpl soft-deletion tests.
- Added WalletController soft-deletion tests.
- Added Cucumber scenarios for wallet deletion and access protection.
- Added Cucumber scenarios for transactions involving deleted wallets.
- Added Cucumber scenarios for transfers involving deleted wallets.
- Verified transaction-history preservation.

## API Endpoint

| Method | Endpoint | Description |
|---|---|---|
| DELETE | `/api/wallets/{userId}` | Soft-delete a wallet |

### Successful Response

HTTP Status: `204 No Content`

## Verification

Commands executed successfully:

```powershell
.\gradlew.bat clean test
.\gradlew.bat build