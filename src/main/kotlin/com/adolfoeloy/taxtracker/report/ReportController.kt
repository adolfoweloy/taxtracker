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
    private val balanceReportService: BalanceReportService
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
        val balanceBefore = balanceReportService.getBalanceReport(previousDate.monthValue, previousDate.year)
        val balanceNow = balanceReportService.getBalanceReport(month, year)

        // Add the result to the model to be used in the view with formatted dates
        model.addAttribute("balanceBeforeAt", previousDate.format(dateFormatter))
        model.addAttribute("balanceBefore", balanceBefore)
        model.addAttribute("balanceNowAt", currentDate.format(dateFormatter))
        model.addAttribute("balanceNow", balanceNow)

        return "report"
    }

}