# VGBL income calculation

How the VGBL Summary turns quota prices and my transactions into a monthly income figure, and how those months add up to a period total.

## The calculation

For each selected fund and each month in the period:

```
lastPricingDay    = last day with a quota price in the previous month
pricingDay        = last day with a quota price in this month
                    (or the redemption date, in the month of a full redemption)

lastMonthValue    = quotasHeld(lastPricingDay) × quotaPrice(lastPricingDay)
currentMonthValue = quotasHeld(pricingDay)     × quotaPrice(pricingDay)

netFlow           = redemptions − contributions between the two pricing days

income            = rate(pricingDay) × (currentMonthValue − lastMonthValue + netFlow)
```

The fund's total is the sum of its monthly incomes, and the grand total is the sum across funds.

### Pricing day

The last day of the month that has a quota price. CVM only publishes business days, so this is often not the calendar month end.

In the month of a full redemption, the pricing day is the redemption date and nothing after it is reported. See [redemption dates](redemption-dates.md).

### Quotas held

The running total of contributions minus redemptions recorded for the fund, as of the pricing day:

- a contribution counts from its own date;
- a redemption counts from the day **after** its date, because the redemption date is still priced with the quotas held before it.

A fund with no transaction recorded before the pricing day falls back to the quota count stored on the fund itself.

### Net flow

Money that moved quotas in or out between the two pricing days (`lastPricingDay` and `pricingDay`) is not income. 
Without it (net flow), a partial redemption would look like a loss and a contribution like a gain.

The change in value between the two pricing days has two causes:

1. the quota price moved: this is income;
2. the number of quotas changed because I bought or redeemed some: this is my own money going in or out, not income.

The net flow removes the second cause. For example, in November of the [worked example](#worked-example):

```
Oct 31 (lastPricingDay)  1,000 quotas × 1.00 = 1,000
Nov 10                   redeem 100 quotas × 1.02 (that day's quota price) = 102 received
Nov 28 (pricingDay)        900 quotas × 1.05 =   945

value change   945 − 1,000       = −55   (looks like a loss)
income         945 − 1,000 + 102 =  47   (what the fund actually earned)
```

The value dropped by 55, but 102 of that left as cash in my hand. A contribution works the other way round: if I put in 200 during the month, the value rises by at least 200 even if the price doesn't move, so the 200 is subtracted.

A redemption pays the quotas redeemed × the quota price on its valuation date, gross of Brazilian tax. That gross amount is what gets added back, which keeps the income gross as the ATO requires.

Which day a flow is counted in:

- A redemption is counted when it falls on or after the previous pricing day and before this one.
- A contribution is counted when it falls after the previous pricing day and on or before this one.

A redemption on the pricing day itself is left for the following month. That is what stops a full redemption from being added back in its exit month.

### Forex

Everything in the month (both values and the net flow) is converted at the rate of the month's **pricing day**. A month's income in AUD is therefore its BRL income times one rate, and exchange rate movements between months never show up as income. See [forex rate lookup](../forex/rate-lookup.md).

### Period

The period is "From" inclusive to "To" exclusive: choosing December as "To" makes November the last month reported. The month before "From" is also loaded, only to give the first month something to subtract from. It is not reported.

## Worked example

Made-up numbers. 1,000 quotas held; 10% redeemed in November, 40% in early December, the rest on Dec 20 as a full redemption.

| Date | Quota price | Event | Quotas held after |
|---|---|---|---|
| Oct 31 | 1.00 | | 1,000 |
| Nov 10 | 1.02 | redeem 100 quotas, gross 102 | 900 |
| Nov 28 | 1.05 | November's pricing day | 900 |
| Dec 5 | 1.06 | redeem 400 quotas, gross 424 | 500 |
| Dec 20 | 1.08 | redeem 500 quotas, gross 540 (full redemption) | 0 |

| Month | Pricing day | Value | Previous value | Net flow | Income (BRL) |
|---|---|---|---|---|---|
| Nov | Nov 28 | 900 × 1.05 = 945 | 1,000 | +102 | **47** |
| Dec | Dec 20 | 500 × 1.08 = 540 | 945 | +424 (Dec 20 is left out) | **19** |
| **Total** | | | | | **66** |

Check: 102 + 424 + 540 taken out, minus 1,000 at the start, is 66.

With a single holding and no transactions in the period, the net flow is zero and each month is simply `quotas × (this month's price − last month's price)`.

## Known limitations

- The Fund Performance report's monthly return percentage ignores the net flow, so a month with a partial redemption shows a misleading return.
- The net flow uses the amount recorded on each transaction. If that amount includes fees, or is net of Brazilian tax, the difference leaks into the month's income.

Both are tracked in GitHub issue #13.

## Where in the code

- `VGBLFundRepository.getIncomeDifferenceByCompetenceDate`: pricing day, quotas held, values and net flow (native SQL).
- `VGBLFundService.getIncomeDataForPeriod`: forex conversion and the income formula.
- `VGBLSummaryPageController`: totals per fund and across funds.
