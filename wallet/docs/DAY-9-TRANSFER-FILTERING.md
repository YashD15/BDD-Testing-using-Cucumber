# Day 9 — Transfer History Filtering

## Goal

Allow users to filter transfer history by status and date range
while retaining pagination.

## Implemented

- Created `TransferStatus` enum.
- Created `TransferHistoryFilterDto`.
- Added optional status and date filters to the repository query.
- Updated the service to validate and normalize filters.
- Added inclusive date-range filtering.
- Exposed filters through the transfer history API.
- Added service and controller tests for filtering and validation.
- Preserved newest-first ordering and pagination metadata.

## API Examples

All transfers for a user:

    GET /api/transfers?userId=101

Completed transfers:

    GET /api/transfers?userId=101&status=COMPLETED

Transfers during October 2026:

    GET /api/transfers?userId=101&startDate=2026-10-01&endDate=2026-10-31

Combined filters and pagination:

    GET /api/transfers?userId=101&status=COMPLETED&startDate=2026-10-01&endDate=2026-10-31&page=0&size=5

## Validation Rules

- Supported statuses: COMPLETED, FAILED, PENDING.
- Status matching is case-insensitive.
- Start and end dates are optional.
- Start date cannot be after end date.
- Both date boundaries include the selected calendar dates.
- Page numbering starts at zero.
- Page size must be between 1 and 100.

## Verification

    .\gradlew.bat clean test
    .\gradlew.bat build

## Key Learnings

- Optional query parameters support flexible API filtering.
- Repository queries can combine filters with pagination.
- LocalDate is useful for calendar-date filtering.
- DTOs keep filtering inputs separate from persistence entities.