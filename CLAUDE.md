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

### Testing

- **Unit tests** (`src/test/`): JUnit 5 + Mockito (mockito-kotlin). Use **AssertJ** for assertions.
- **Integration tests** (`src/integrationTest/`): Full Spring context with **Testcontainers** PostgreSQL. Separate Gradle source set with its own configuration.

## Coding Conventions

- Follow Kotlin coding conventions: https://kotlinlang.org/docs/coding-conventions.html
- Use idiomatic Kotlin (type inference, extension functions, data classes).
- Use AssertJ for all test assertions.
- When mocking, use mockito-kotlin.
