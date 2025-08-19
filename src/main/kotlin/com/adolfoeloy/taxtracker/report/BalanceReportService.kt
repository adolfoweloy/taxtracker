package com.adolfoeloy.taxtracker.report

import com.adolfoeloy.taxtracker.balance.BalanceRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class BalanceReportService(
    private val balanceRepository: BalanceRepository
) {

    fun getBalanceReport(month: Int, year: Int): List<BalanceReport> {

        balanceRepository.findByMonthAndYear(month, year).let { balances ->
            return balances.map { balance ->
                BalanceReport(
                    product = balance.product?.name ?: "Unknown Product",
                    certificate = balance.product?.certificate ?: "Unknown Certificate",
                    issuedAt = balance.product?.issuedAt?.toString() ?: "Unknown Issue Date",
                    maturityDate = balance.product?.matureAt?.toString() ?: "Unknown Maturity Date",
                    principal = balance.principal.fromCents(),
                    interest = balance.interest.fromCents(),
                    estimatedBrlTax = balance.brTax.fromCents()
                )
            }
        }

    }

    fun Int.fromCents(): String {
        return BigDecimal.valueOf(this.toLong())
            .movePointLeft(2)
            .setScale(2, RoundingMode.HALF_UP) // garante 2 casas
            .toPlainString()
    }
}