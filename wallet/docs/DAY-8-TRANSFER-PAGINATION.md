# Day 8 — Transfer History Pagination

## Goal

Support paginated retrieval of incoming and outgoing wallet transfers.

## Implemented

- Updated `WalletTransferRepository` to return a `Page`.
- Added a count query for pagination metadata.
- Updated `TransferService` to accept page and size parameters.
- Validated page numbers and page sizes.
- Updated `TransferController` to expose pagination parameters.
- Added tests for paginated results, empty history, and invalid pagination.

## API Examples

First page with default settings:

    GET /api/transfers?userId=101

Second page with 10 records per page:

    GET /api/transfers?userId=101&page=1&size=10

First page with 5 records:

    GET /api/transfers?userId=101&page=0&size=5

## Pagination Rules

- Page numbering starts at zero.
- Default page is `0`.
- Default page size is `10`.
- Allowed page sizes are from `1` to `100`.
- Results are sorted newest first.

## Response Metadata

The response includes:

- `content`: transfers on the requested page.
- `totalElements`: total matching transfers.
- `totalPages`: number of pages available.
- `number`: current page index.
- `size`: requested page size.
- `first` and `last`: whether this is the first or last page.

## Verification

    .\gradlew.bat clean test
    .\gradlew.bat build

## Key Learnings

- Spring Data's `Pageable` represents pagination and sorting.
- `Page<T>` provides results together with pagination metadata.
- `Page.map()` converts entities to DTOs without losing page information.
