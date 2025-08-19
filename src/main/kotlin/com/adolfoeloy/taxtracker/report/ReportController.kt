package com.adolfoeloy.taxtracker.report

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Controller
@RequestMapping("/report")
class ReportController(
    private val reportService: ReportService
) {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    @GetMapping("/index/{month}/{year}")
    fun index(
        model: Model,
        @PathVariable month: Int,
        @PathVariable year: Int
    ): String {
        val currentDate = LocalDate.of(year, month, 1)
        val previousDate = currentDate.minusDays(1)
        val balanceBefore = reportService.getBalanceReport(previousDate.monthValue, previousDate.year)
        val balanceNow = reportService.getBalanceReport(month, year)

        val transactions = reportService.getTransactionReport(month, year)

        // Add the result to the model to be used in the view with formatted dates
        model.addAttribute("period", Period(
            from = currentDate.format(dateFormatter),
            to = currentDate.withDayOfMonth(currentDate.lengthOfMonth()).format(dateFormatter)
        ))

        model.addAttribute("balanceBefore", Balance(
            balanceAt = previousDate.format(dateFormatter),
            entries = balanceBefore
        ))

        model.addAttribute("balanceNow", Balance(
            balanceAt = currentDate.format(dateFormatter),
            entries = balanceNow
        ))

        model.addAttribute("transactions", Transactions(
            entries = transactions
        ))

        return "report"
    }

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

        val totalBrToAuForex: Int
            get() = entries.sumOf { it.brToAuForex }
    }

}