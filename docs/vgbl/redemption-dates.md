# Redemption dates

Which date is recorded for a VGBL redemption, and how the income calculation uses it.

## Valuation date, not credit date

A redemption goes through three dates:

- **Request date:** the day the redemption is requested.
- **Valuation date:** the day the redeemed quotas are priced, at that day's quota price.
- **Credit date:** the day the money reaches the bank account. This is the date printed on the redemption receipt.

Each fund's rules set the gaps between them in business days: a conversion delay (cotização) from request to valuation, and a payment delay (liquidação) from valuation to credit. Some funds price on the request day itself; others price days later. For example, with a D+3 conversion and a D+3 payment:

```
D      request
D+3    valuation: quotas priced at this day's quota price
D+6    credit
```

A request placed after the fund's daily cutoff time counts as the next business day's request. The delays are stated in the fund's lâmina or regulamento on the CVM site.

The transaction date recorded for a redemption is the **valuation date**. It is the only date whose quota price reproduces the gross amount on the receipt: quotas redeemed × that day's quota price = gross amount. Any other day in the fortnight is off.

When recording a new redemption, find the valuation date this way: divide the receipt's gross amount by the quotas redeemed, and look for the day whose CVM quota price matches. If no day matches exactly, start from the request date, add the fund's conversion delay, and check that the result is the closest match.

## How the income calculation uses it

The valuation date is the last day the fund counts as held:

- In the month of a **full redemption**, that month's pricing day is the redemption date, and later months are not reported.
- On the redemption date itself, the fund is still priced with the quotas held **before** the redemption. Otherwise the exit month would be priced at zero and show the whole holding as a loss.
- The quota price for the valuation date must have been imported. If it is missing, the exit month is silently priced on the closest earlier day.

A **partial redemption** does not end the reporting. The quotas held drop from the day after, and the redeemed amount is added back to that month's income as net flow. See [income calculation](income-calculation.md).

## Not handled here

The redemption receipt also states a taxable income figure that differs from the income declared month by month. Reconciling the two is tracked separately in GitHub issue #14.

## Where in the code

- `VGBLFundRepository.getIncomeDifferenceByCompetenceDate`: the inclusive cutoff at a full redemption, and redemptions counting from the day after.
