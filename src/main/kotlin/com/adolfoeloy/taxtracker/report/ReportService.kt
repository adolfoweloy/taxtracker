package com.adolfoeloy.taxtracker.report

import com.adolfoeloy.taxtracker.balance.BalanceRepository
import com.adolfoeloy.taxtracker.forex.ForexService
import com.adolfoeloy.taxtracker.transaction.TransactionRepository
import com.adolfoeloy.taxtracker.util.toYearMonthString
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class ReportService(
    private val balanceRepository: BalanceRepository,
    private val transactionRepository: TransactionRepository,
    private val forexService: ForexService,
    private val taxProperties: TaxProperties
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
        val lastDayOfMonth = LocalDate.of(year, month, getLastDayOf(year, month))

        return balanceRepository.findByMonthAndYear(month, year).map { balance ->
            BalanceReport(
                product = balance.product?.name ?: "Unknown Product",
                certificate = balance.product?.certificate?.value ?: "Unknown Certificate",
                issuedAt = balance.product?.issuedAt?.format(dateFormatter) ?: "Unknown Issue Date",
                maturityDate = balance.product?.matureAt?.format(dateFormatter) ?: "Unknown Maturity Date",
                principal = forexService.applyForexRateFor(balance.principal, lastDayOfMonth, currency),
                interest = forexService.applyForexRateFor(balance.interest, lastDayOfMonth, currency),
                estimatedBrlTax = forexService.applyForexRateFor(balance.brTax, lastDayOfMonth, currency)
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
                principal = forexService.applyForexRateFor(transaction.principal, transaction.paymentDate, currency),
                redemption = forexService.applyForexRateFor(transaction.redemption, transaction.paymentDate, currency),
                interest = forexService.applyForexRateFor(transaction.interest, transaction.paymentDate, currency),
                brTax = forexService.applyForexRateFor(transaction.brTax, transaction.paymentDate, currency),
                credit = forexService.applyForexRateFor(transaction.credited, transaction.paymentDate, currency),
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
                        transaction -> forexService.applyForexRateFor(transaction.interest, transaction.paymentDate, currency)
                    }

            // there are months that I can't figure out the right figures, so unfortunately I skip them and can't claim FITO
            val totalPaidTax = if (taxProperties.skipPaidFor.contains(tmpCurrent.toYearMonthString())) {
                0
            } else {
                transactionRepository
                    .findByMonthAndYear(tmpCurrent.monthValue, tmpCurrent.year)
                    .sumOf {
                            transaction -> forexService.applyForexRateFor(transaction.brTax, transaction.paymentDate, currency)
                    }
            }

            // balance difference
            val previousMonthLastDay = LocalDate.of(previousMonth.year, previousMonth.monthValue, getLastDayOf(previousMonth.year, previousMonth.monthValue))
            val currentMonthLastDay = LocalDate.of(tmpCurrent.year, tmpCurrent.monthValue, getLastDayOf(tmpCurrent.year, tmpCurrent.monthValue))

            val previousBalanceInterest = previousBalance.sumOf {
                forexService.applyForexRateFor(it.interest, previousMonthLastDay, currency)
            }
            val currentBalanceInterest = currentBalance.sumOf {
                forexService.applyForexRateFor(it.interest, currentMonthLastDay, currency)
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
