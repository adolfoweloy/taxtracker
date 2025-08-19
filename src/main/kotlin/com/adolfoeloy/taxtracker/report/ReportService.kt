package com.adolfoeloy.taxtracker.report

import com.adolfoeloy.taxtracker.balance.BalanceRepository
import com.adolfoeloy.taxtracker.transaction.TransactionRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter

@Component
class ReportService(
    private val balanceRepository: BalanceRepository,
    private val transactionRepository: TransactionRepository
) {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun getBalanceReport(month: Int, year: Int): List<BalanceReport> {
        return balanceRepository.findByMonthAndYear(month, year).map { balance ->
            BalanceReport(
                product = balance.product?.name ?: "Unknown Product",
                certificate = balance.product?.certificate ?: "Unknown Certificate",
                issuedAt = balance.product?.issuedAt?.format(dateFormatter) ?: "Unknown Issue Date",
                maturityDate = balance.product?.matureAt?.format(dateFormatter) ?: "Unknown Maturity Date",
                principal = balance.principal.fromCents(),
                interest = balance.interest.fromCents(),
                estimatedBrlTax = balance.brTax.fromCents()
            )
        }
    }

    fun getTransactionReport(month: Int, year: Int): List<TransactionReport> {
        return transactionRepository.findByMonthAndYear(month, year).map { transaction ->
            TransactionReport(
                product = transaction.product?.name ?: "Unknown Product",
                certificate = transaction.product?.certificate ?: "Unknown Certificate",
                issuedAt = transaction.product?.issuedAt?.format(dateFormatter) ?: "Unknown Issue Date",
                matureAt = transaction.product?.matureAt?.format(dateFormatter) ?: "Unknown Maturity Date",
                paymentAt = transaction.paymentDate.format(dateFormatter),
                principal = transaction.principal.fromCents(),
                redemption = transaction.redemption.fromCents(),
                interest = transaction.interest.fromCents(),
                brTax = transaction.brTax.fromCents(),
                credit = transaction.credited.fromCents(),
                description = transaction.description,
                brToAuForex = transaction.brToAuForex.fromCents()
            )
        }
    }

    private fun Int.fromCents(): String {
        return BigDecimal.valueOf(this.toLong())
            .movePointLeft(2)
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString()
    }
}
