package com.adolfoeloy.taxtracker.report

import com.adolfoeloy.taxtracker.balance.BalanceRepository
import com.adolfoeloy.taxtracker.forex.ForexService
import com.adolfoeloy.taxtracker.transaction.TransactionRepository
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class ReportService(
    private val balanceRepository: BalanceRepository,
    private val transactionRepository: TransactionRepository,
    private val forexService: ForexService
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun getReportData(month: Int, year: Int, currency: String): ReportData {
        val balanceBefore = getBalanceReport(month - 1, year, currency)
        val balanceNow = getBalanceReport(month, year, currency)
        val transactions = getTransactionsReport(month, year, currency)

        return ReportData(
            period = Period(
                from = "${year}-${month}-01",
                to = "${year}-${month}-${getLastDayOf(year, month)}"
            ),
            balanceBefore = Balance(balanceAt = "${year}-${month - 1}-01", entries = balanceBefore),
            balanceNow = Balance(balanceAt = "${year}-${month}-01", entries = balanceNow),
            transactions = Transactions(entries = transactions),
            currencyTicker = currency
        )
    }

    private fun getLastDayOf(year: Int, month: Int): Int = LocalDate.of(year, month, 1).lengthOfMonth()

    private fun getBalanceReport(month: Int, year: Int, currency: String): List<BalanceReport> {
        return balanceRepository.findByMonthAndYear(month, year).map { balance ->
            BalanceReport(
                product = balance.product?.name ?: "Unknown Product",
                certificate = balance.product?.certificate?.value ?: "Unknown Certificate",
                issuedAt = balance.product?.issuedAt?.format(dateFormatter) ?: "Unknown Issue Date",
                maturityDate = balance.product?.matureAt?.format(dateFormatter) ?: "Unknown Maturity Date",
                principal = forexService.applyForexRateFor(balance.principal, currency),
                interest = forexService.applyForexRateFor(balance.interest, currency),
                estimatedBrlTax = forexService.applyForexRateFor(balance.brTax, currency)
            )
        }
    }

    private fun getTransactionsReport(month: Int, year: Int, currency: String): List<TransactionReport> {
        return transactionRepository.findByMonthAndYear(month, year).map { transaction ->
            TransactionReport(
                product = transaction.product?.name ?: "Unknown Product",
                certificate = transaction.product?.certificate?.value ?: "Unknown Certificate",
                issuedAt = transaction.product?.issuedAt?.format(dateFormatter) ?: "Unknown Issue Date",
                matureAt = transaction.product?.matureAt?.format(dateFormatter) ?: "Unknown Maturity Date",
                paymentAt = transaction.paymentDate.format(dateFormatter),
                principal = forexService.applyForexRateFor(transaction.principal, currency),
                redemption = forexService.applyForexRateFor(transaction.redemption, currency),
                interest = forexService.applyForexRateFor(transaction.interest, currency),
                brTax = forexService.applyForexRateFor(transaction.brTax, currency),
                credit = forexService.applyForexRateFor(transaction.credited, currency),
                description = transaction.description,
                brToAuForex = transaction.brToAuForex
            )
        }
    }

    fun getTaxReportData(start: LocalDate, end: LocalDate, currency: String = "BRL"): List<TaxReport> {
        val result = mutableListOf<TaxReport>()

        var previousMonth = start.minusMonths(1)
        var tmpCurrent = start

        while (tmpCurrent.isBefore(end) || tmpCurrent.isEqual(end)) {

            // not always the same year
            val previousBalance = balanceRepository.findByMonthAndYear(
                month = previousMonth.monthValue,
                year = previousMonth.year
            )

            val currentBalance = balanceRepository.findByMonthAndYear(
                month = tmpCurrent.monthValue,
                year = tmpCurrent.year
            )

            val totalInterest = transactionRepository
                .findByMonthAndYear(tmpCurrent.monthValue, tmpCurrent.year)
                    .sumOf {
                        transaction -> forexService.applyForexRateFor(transaction.interest, currency)
                    }

            val totalPaidTax = transactionRepository
                .findByMonthAndYear(tmpCurrent.monthValue, tmpCurrent.year)
                    .sumOf {
                        transaction -> forexService.applyForexRateFor(transaction.brTax, currency)
                    }

            // balance difference
            val previousBalanceInterest = previousBalance.sumOf {
                forexService.applyForexRateFor(it.interest, currency)
            }
            val currentBalanceInterest = currentBalance.sumOf {
                forexService.applyForexRateFor(it.interest, currency)
            }
            val totalGrossInterest = (currentBalanceInterest + totalInterest) - previousBalanceInterest

            result.add(
                TaxReport(
                    calendarPeriod = CalendarPeriod(
                        month = tmpCurrent.monthValue,
                        year = tmpCurrent.year
                    ),
                    totalGrossInterestEarned = totalGrossInterest,
                    totalPaidTax = totalPaidTax,
                    currencyTicker = currency
                )
            )

            // step on the next month
            previousMonth = previousMonth.plusMonths(1)
            tmpCurrent = tmpCurrent.plusMonths(1)
        }

        return result
    }

}
