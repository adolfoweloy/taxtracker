package com.adolfoeloy.taxtracker.report

import com.adolfoeloy.taxtracker.util.fromYearMonthString
import com.adolfoeloy.taxtracker.util.lastDay
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Controller
@RequestMapping("/report")
class ReportController(
    private val reportService: ReportService
) {

    private val yearMonthFormatter = DateTimeFormatter.ofPattern("yyyyMM")

    @GetMapping("/{month}/{year}")
    fun interestByMonthAndYear(
        model: Model,
        @RequestParam("currency", required = false, defaultValue = "BRL") currency: String,
        @PathVariable month: Int,
        @PathVariable year: Int
    ): String {

        reportService.getReportData(month, year, currency).let { reportData ->
            model.addAttribute("period", reportData.period)
            model.addAttribute("balanceBefore", reportData.balanceBefore)
            model.addAttribute("balanceNow", reportData.balanceNow)
            model.addAttribute("transactions", reportData.transactions)
            model.addAttribute("totalGrossInterestEarned", reportData.totalGrossInterestEarned())
            model.addAttribute("currencyTicker", reportData.currencyTicker)
        }

        return "report"
    }

    @GetMapping("/tax/{financial_year}")
    fun interestFYSummary(
        model: Model,
        @RequestParam("currency", required = false, defaultValue = "BRL") currency: String,
        @PathVariable("financial_year") financialYear: Int
    ): String {
        val start = LocalDate.of(2000 + (financialYear - 1), 7, 1)
        val end = LocalDate.of(2000 + financialYear, 6, 30)

        val taxReport = reportService.getTaxReportData(
            start = start,
            end = end,
            currency = currency
        )

        setModel(model, taxReport, financialYear)
        return "tax_report"
    }

    @GetMapping("/installment/{financial_year}")
    fun installmentReport(
        model: Model,
        @PathVariable("financial_year") financialYear: Int,
    ): String {
        val fyStart = YearMonth.of(2000 + financialYear - 1, 7)
        val fyEnd = YearMonth.of(2000 + financialYear, 6)
        val now = YearMonth.now()

        val from = fyStart.format(yearMonthFormatter)
        val to = (if (now < fyEnd) now else fyEnd).format(yearMonthFormatter)

        return buildInstallmentReport(model, financialYear, from, to, "BRL")
    }

    @PostMapping("/installment")
    fun installmentReportPost(
        model: Model,
        @RequestParam("financial_year") financialYear: Int,
        @RequestParam from: String,
        @RequestParam to: String,
        @RequestParam("currency", required = false, defaultValue = "BRL") currency: String,
    ): String {
        val normalizedFrom = from.replace("-", "")
        val normalizedTo = to.replace("-", "")
        return buildInstallmentReport(model, financialYear, normalizedFrom, normalizedTo, currency)
    }

    private fun buildInstallmentReport(
        model: Model,
        financialYear: Int,
        from: String,
        to: String,
        currency: String,
    ): String {
        val start = from.fromYearMonthString()
        val end = to.fromYearMonthString().lastDay()

        val taxReport = reportService.getTaxReportData(
            start = start,
            end = end,
            currency = currency
        )

        setModel(model, taxReport, financialYear)
        model.addAttribute("from", from)
        model.addAttribute("to", to)
        model.addAttribute("currency", currency)
        return "tax_report"
    }

    private fun setModel(
        model: Model,
        taxReport: List<TaxReport>,
        financialYear: Int
    ) {
        model.addAttribute("taxReport", taxReport)
        model.addAttribute("financialYear", financialYear)
        model.addAttribute("totalGrossInterestEarned", taxReport.sumOf { it.totalGrossInterestEarned })
        model.addAttribute("totalPaidTax", taxReport.sumOf { it.totalPaidTax })
    }
}