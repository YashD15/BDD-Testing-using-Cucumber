# Day 12 — Transfer History Filtering and Pagination Tests

## Objective

Verify that transfer history filtering and pagination work correctly
together through the service and REST controller.

## Tests Added

- Combined transfer status and date-range filtering.
- Pagination with a non-zero page index.
- Correct total element and total page counts.
- Controller handling of status, start date, end date, page, and size.
- Verification that filter parameters are passed correctly to the service.

## Filtering Rules

- Status filters match the requested transfer status.
- The start date is inclusive.
- The end date is inclusive at the date level.
- Internally, the end date is converted to the next day's start
  and used as an exclusive upper boundary.

## Pagination Rules

- Page numbering starts at zero.
- Default page size is 10.
- Page size must be between 1 and 100.
- Results are ordered by creation time descending, then transfer ID
  descending for stable ordering.

## API Example

    GET /api/transfers?userId=101&status=COMPLETED&startDate=2026-01-01&endDate=2026-01-31&page=1&size=5

## Verification

Run all tests:

    .\gradlew.bat clean test

Build the project:

    .\gradlew.bat build

## Result

Transfer history filtering and pagination are covered by service and
controller tests, including combined filter scenarios.