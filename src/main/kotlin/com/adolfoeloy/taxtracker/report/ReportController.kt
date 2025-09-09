package com.adolfoeloy.taxtracker.report

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

@Controller
@RequestMapping("/report")
class ReportController(
    private val reportService: ReportService
) {

    @GetMapping("/{month}/{year}")
    fun index(
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
    fun tax(
        model: Model,
        @PathVariable("financial_year") financialYear: Int
    ): String {
        val start = LocalDate.of(2000 + (financialYear - 1), 7, 1)
        val end = LocalDate.of(2000 + financialYear, 6, 30)
        val taxReport = reportService.getTaxReportData(
            start = start,
            end = end
        )
        model.addAttribute("taxReport", taxReport)
        model.addAttribute("financialYear", financialYear)
        model.addAttribute("totalGrossInterestEarned", taxReport.sumOf { it.totalGrossInterestEarned })
        model.addAttribute("totalPaidTax", taxReport.sumOf { it.totalPaidTax })
        return "tax_report"
    }
}