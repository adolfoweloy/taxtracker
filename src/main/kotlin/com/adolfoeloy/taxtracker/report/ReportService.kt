package com.adolfoeloy.taxtracker.report

import com.adolfoeloy.taxtracker.balance.BalanceRepository
import com.adolfoeloy.taxtracker.transaction.TransactionRepository
import org.springframework.stereotype.Component
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
                principal = balance.principal,
                interest = balance.interest,
                estimatedBrlTax = balance.brTax
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
