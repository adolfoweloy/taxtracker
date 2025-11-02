package com.adolfoeloy.taxtracker.report

data class TaxReport(
    val calendarPeriod: CalendarPeriod,
    val totalGrossInterestEarned: Int,
    val totalPaidTax: Int,
    val currencyTicker: String
)

data class CalendarPeriod(
    val month: Int,
    val year: Int
)

data class ReportData(
    val period: Period,
    val balanceBefore: Balance,
    val balanceNow: Balance,
    val transactions: Transactions,
    val currencyTicker: String,
) {
    fun totalGrossInterestEarned(): Int =
        (balanceNow.totalInterest + transactions.totalInterest) - balanceBefore.totalInterest
}

data class Product(
    val name: String,
    val certificate: String,
    val issuedAt: String,
    val matureAt: String)

data class Period(
    val from: String,
    val to: String
)

data class Balance(
    val balanceAt: String,
    val entries: List<BalanceReport>
) {
    val totalPrincipal: Int
        get() = entries.sumOf { it.principal }

    val totalInterest: Int
        get() = entries.sumOf { it.interest }

    val totalEstimatedBrlTax: Int
        get() = entries.sumOf { it.estimatedBrlTax }
}

data class Transactions(
    val entries: List<TransactionReport>
) {
    val totalPrincipal: Int
        get() = entries.sumOf { it.principal }

    val totalRedemption: Int
        get() = entries.sumOf { it.redemption }

    val totalInterest: Int
        get() = entries.sumOf { it.interest }

    val totalBrTax: Int
        get() = entries.sumOf { it.brTax }

    val totalCredit: Int
        get() = entries.sumOf { it.credit }

}
