package com.adolfoeloy.taxtracker.fixture

import com.adolfoeloy.taxtracker.report.Period
import com.adolfoeloy.taxtracker.report.ReportData
import com.adolfoeloy.taxtracker.report.Transactions

class ReportMother {

    companion object {

        fun createJanuaryReport(): ReportData {
            // ~ three years certificate example
            val cdbFacil = BalanceMother.withCDBFacil(
                issuedAt = "2022-04-01",
                maturityDate = "2025-04-30",
                principal = 1_000_000 // 10_000.00
            )

            return ReportData(
                // January 2025 report
                period = Period(
                    from = "2025-01-01",
                    to = "2025-01-31"
                ),
                // Balances at the start and end of the month
                balanceBefore = cdbFacil.withBalance(
                    balanceAt = "2024-12-31",
                    interest = 500_000, // 5_000.00
                    estimatedBrlTax = 75_000 // 750.00 (15% of interest)
                ),
                balanceNow = cdbFacil.withBalance(
                    balanceAt = "2025-01-31",
                    interest = 550_000, // 5_500.00
                    estimatedBrlTax = 82_500 // 825.00 (15% of interest, rounded up)
                ),
                transactions = Transactions(
                    entries = listOf(
                        TransactionMother.withdrawFromCdbFacilAt(
                            product = cdbFacil.product,
                            paymentAt = "2025-01-22",
                            amount = 20_000, // amount withdrawn is 200.00
                            interest = 2_500, // interest portion is 25.00
                            tax = 375 // tax portion is 3.75
                        ),
                        TransactionMother.withdrawFromCdbFacilAt(
                            product = cdbFacil.product,
                            paymentAt = "2025-01-16",
                            amount = 10_000, // amount withdrawn is 100.00
                            interest = 1_500, // interest portion is 15.00
                            tax = 225 // tax portion is 2.25
                        )
                    )
                ),
                currencyTicker = "BRL"
            )
        }
    }
}