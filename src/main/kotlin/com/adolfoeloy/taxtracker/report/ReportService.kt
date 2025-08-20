package com.adolfoeloy.taxtracker.report

import com.adolfoeloy.taxtracker.balance.BalanceRepository
import com.adolfoeloy.taxtracker.transaction.TransactionRepository
import com.adolfoeloy.taxtracker.util.fromCents
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class ReportService(
    private val balanceRepository: BalanceRepository,
    private val transactionRepository: TransactionRepository
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
            transactions = Transactions(entries = transactions)
        )
    }

    private fun getLastDayOf(year: Int, month: Int): Int = LocalDate.of(year, month, 1).lengthOfMonth()

    private fun getBalanceReport(month: Int, year: Int, currency: String): List<BalanceReport> {
        return balanceRepository.findByMonthAndYear(month, year).map { balance ->
            BalanceReport(
                product = balance.product?.name ?: "Unknown Product",
                certificate = balance.product?.certificate ?: "Unknown Certificate",
                issuedAt = balance.product?.issuedAt?.format(dateFormatter) ?: "Unknown Issue Date",
                maturityDate = balance.product?.matureAt?.format(dateFormatter) ?: "Unknown Maturity Date",
                principal = balance.principal,
                interest = balance.interest,
                estimatedBrlTax = balance.brTax
            )
        }
    }

    private fun getTransactionsReport(month: Int, year: Int, currency: String): List<TransactionReport> {
        return transactionRepository.findByMonthAndYear(month, year).map { transaction ->
            TransactionReport(
                product = transaction.product?.name ?: "Unknown Product",
                certificate = transaction.product?.certificate ?: "Unknown Certificate",
                issuedAt = transaction.product?.issuedAt?.format(dateFormatter) ?: "Unknown Issue Date",
                matureAt = transaction.product?.matureAt?.format(dateFormatter) ?: "Unknown Maturity Date",
                paymentAt = transaction.paymentDate.format(dateFormatter),
                principal = transaction.principal,
                redemption = transaction.redemption,
                interest = transaction.interest,
                brTax = transaction.brTax,
                credit = transaction.credited,
                description = transaction.description,
                brToAuForex = transaction.brToAuForex
            )
        }
    }

}
