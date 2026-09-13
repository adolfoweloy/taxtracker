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
- **VGBL exit month is bounded by the redemption's valuation date, not its credit date:** `VGBLFundRepository`'s query cuts income off at `vgbl_track.transaction_date` and `DISTINCT ON` keeps the *latest* surviving row of each month, so the exit month ends on whichever quota row is nearest below the cutoff. A redemption is priced at its valuation quota (D+1 of the request) but credited several business days later, so those two dates pick different rows. For the TRUXT MACRO exit the valuation date was 2025-11-04 — that row had never been imported, and the stored 11-03 and 11-06 values did not match the CVM file. Fixed by importing 11-04 and deleting 11-03, 11-06 and 11-07. Post-exit rows need no cleanup: the cutoff already ignores them, and an import only ever writes the file's latest day (`CsvCvmFundDataImpl.loadFrom`).

### Testing

- **Unit tests** (`src/test/`): JUnit 5 + Mockito (mockito-kotlin). Use **AssertJ** for assertions.
- **Integration tests** (`src/integrationTest/`): Full Spring context with **Testcontainers** PostgreSQL. Separate Gradle source set with its own configuration.

## Coding Conventions

- Follow Kotlin coding conventions: https://kotlinlang.org/docs/coding-conventions.html
- Use idiomatic Kotlin (type inference, extension functions, data classes).
- Use AssertJ for all test assertions.
- When mocking, use mockito-kotlin.
