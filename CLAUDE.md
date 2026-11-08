# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Summary

Spring Boot 3.4.4 + Kotlin 1.9.25 web app that converts Bradesco banking reports (BRL) to AUD and calculates interest-based earnings for Australian Tax Office reporting. Personal-use tool, not seeking contributions.

## Build & Run Commands

```bash
# Start PostgreSQL (required for running the app)
docker-compose up -d

# Run the application
./gradlew bootRun

# Run unit tests
./gradlew test

# Run a single unit test class
./gradlew test --tests "com.adolfoeloy.taxtracker.SomeTestClass"

# Run integration tests (uses Testcontainers, requires Docker)
./gradlew integrationTest

# Build without tests
./gradlew build -x test
```

**Environment variables** for forex conversion:
- `CURRENCYBEACON_API_KEY` — API key for CurrencyBeacon
- `CURRENCYBEACON_ENABLED` — set to `true` to use the external forex API (defaults to `false`, which uses `LocalForexProvider`)

## Architecture

### Domain Packages (`com.adolfoeloy.taxtracker`)

- **balance** — CSV import of monthly CDB balances (interest, principal, taxes, forex rates). `BalanceImportService` parses CSV and persists via `BalanceRepository`.
- **transaction** — CSV import of transaction records (withdrawals, interest payments, taxes). Same import pattern as balance.
- **product** — Financial products (CDB certificates) with issue/maturity dates. Products are auto-created during balance/transaction imports.
- **report** — Monthly income reports and FY tax summaries. `ReportService` aggregates balance/transaction data and applies forex conversion. `ReportController` serves Thymeleaf views.
- **forex** — Exchange rate abstraction. `ForexService` interface with `DefaultForexService` implementation.
- **forex/provider** — Provider pattern configured via `ForexProviderConfiguration` using `@ConditionalOnProperty`. When `CURRENCYBEACON_ENABLED=true`, uses `CurrencyBeaconProvider` wrapped in `ForexProviderDBDecorator` (caches rates in DB). When `false`, uses `LocalForexProvider`.
- **vgbl** — VGBL fund tracking, imports data from CVM (Brazilian securities commission).
- **properties** — `TaxProperties` typed config including `skipPaidFor` months list.
- **util** — Kotlin extension functions for date/number formatting and cents-to-BigDecimal conversion.

### Key Design Decisions

- **Amounts as integers (cents):** Monetary values stored as `Int` in the database to avoid floating-point issues. Extension function `fromCentsToBigDecimal()` for display. There is an ongoing migration toward `BigDecimal` in the `ForexService` interface.
- **Australian Financial Year:** July–June. Reports group by FY accordingly.
- **Flyway migrations:** Schema managed in `src/main/resources/db/migration/` (V001–V006).
- **Thymeleaf layout dialect:** `layout.html` is the master template with sidebar navigation. Views extend it.
- **VGBL income is priced with quotas held as of each competence date, not a static count:** `VGBLFundRepository`'s query used to multiply every month by `fund.quotas`, a single snapshot of the current holding, which is only correct when the holding never changed within the queried period. A running balance is now derived from `vgbl_track` (signed cumulative sum of contributions and redemptions ordered by transaction date) and matched to each competence date, so a partial contribution or redemption mid-period reprices only the months from that point on. `fund.quotas` is kept as a fallback for a competence date with no preceding `vgbl_track` row, so funds without full track history behave as before.
- **A redemption's `vgbl_track.transaction_date` is its valuation date, not its credit date:** the bank values a redemption on the day the request is formalised but credits it several business days later. The valuation date is the one whose quota prices the redemption, so it is what gets recorded: it is the only date whose quota reproduces the gross on the bank's redemption statement. `VGBLFundRepository`'s query treats that date as the last day the fund was held: the cutoff is inclusive, and the running quota balance only counts a redemption from the day after, so the exit day is priced with the quotas held before it rather than zero. `DISTINCT ON` keeps the latest surviving row of each month, so the valuation date's quota row must exist or the exit month falls back to an earlier row. Post-exit rows need no cleanup: the cutoff already ignores them.

### Testing

- **Unit tests** (`src/test/`): JUnit 5 + Mockito (mockito-kotlin). Use **AssertJ** for assertions.
- **Integration tests** (`src/integrationTest/`): Full Spring context with **Testcontainers** PostgreSQL. Separate Gradle source set with its own configuration.

## Coding Conventions

- Follow Kotlin coding conventions: https://kotlinlang.org/docs/coding-conventions.html
- Use idiomatic Kotlin (type inference, extension functions, data classes).
- Use AssertJ for all test assertions.
- When mocking, use mockito-kotlin.

## Privacy

- Never share a figure from the database. You should never expose any personal data in commit messages or pull-requests
- Never share a figure from the database in tests or comments in the codebase
- Never share a figure from the database in READMEs or any other documentation in this codebase
