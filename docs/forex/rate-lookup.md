# Forex rate lookup

How a BRL amount is converted to another currency (AUD in practice), and which rate is used.

## Which rate

A conversion asks for the BRL→target rate on one exact date and multiplies the amount by it. Each report decides which date to ask for; the VGBL Summary uses each month's pricing day (see [VGBL income calculation](../vgbl/income-calculation.md#forex)).

Converting to BRL always uses a rate of 1.

## Where the rate comes from

Controlled by the `CURRENCYBEACON_ENABLED` environment variable.

**`true`:**

1. Look for a BRL→target rate on that exact date in the `exchange_rate` table.
2. If there is none, call CurrencyBeacon's historical endpoint for that date and **save** the result in `exchange_rate`, so the next lookup is served from the database.

There is no fallback to a nearby date: whatever CurrencyBeacon returns for that date is used.

**`false` (the default):** a fixed AUD rate, whatever the date. Useful for running the app without the external API, but the AUD figures it produces are not real.

Because the `true` path writes to the database, simply opening a report in AUD can add rows to `exchange_rate`.

## Where in the code

- `DefaultForexService`: applies the rate to an amount.
- `ForexProviderConfiguration`: picks the provider from `CURRENCYBEACON_ENABLED`.
- `ForexProviderDBDecorator`: the database cache in front of `CurrencyBeaconProvider`.
- `LocalForexProvider`: the fixed rate.
