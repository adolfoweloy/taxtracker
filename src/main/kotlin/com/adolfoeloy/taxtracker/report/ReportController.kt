package com.adolfoeloy.taxtracker.report

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

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
        }

        return "report"
    }

}