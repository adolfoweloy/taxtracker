package com.adolfoeloy.taxtracker.vgbl

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * "Rentabilidade do Fundo" — month-by-month percentage return for a single VGBL fund over one
 * Australian FY, plus the compounded FY figure.
 *
 * A GET with query params rather than the POST form used by [VGBLSummaryPageController]: this
 * screen submits two scalars, so the result stays bookmarkable and survives a refresh.
 */
@Controller
@RequestMapping("/report/fund-performance")
class FundPerformancePageController(
    private val vgblFundRepository: VGBLFundRepository,
    private val vgblFundService: VGBLFundService
) {

    @GetMapping
    fun performancePage(
        model: Model,
        @RequestParam(required = false) cnpj: String?,
        @RequestParam(required = false) fy: Int?
    ): String {
        model.addAttribute("funds", vgblFundRepository.findAll())
        model.addAttribute("financialYears", vgblFundService.getAvailableFinancialYears())
        model.addAttribute("selectedCnpj", cnpj)
        model.addAttribute("selectedFy", fy)

        if (cnpj.isNullOrBlank() || fy == null) {
            return "fund_performance"
        }

        val performance = vgblFundService.getFundPerformanceForFY(cnpj, fy)

        if (performance == null) {
            model.addAttribute("errorMessage", "No fund registered for CNPJ $cnpj")
        } else {
            model.addAttribute("performance", performance)
        }

        return "fund_performance"
    }
}
