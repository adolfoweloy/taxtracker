package com.adolfoeloy.taxtracker.vgbl

import com.adolfoeloy.taxtracker.util.firstDayFromYearMonth
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.math.BigDecimal
import java.math.RoundingMode

@Controller
@RequestMapping("/report/vgbl-summary")
class VGBLSummaryPageController(
    private val vgblFundRepository: VGBLFundRepository,
    private val vgblFundService: VGBLFundService
) {
    private val log: Logger = LoggerFactory.getLogger(VGBLSummaryPageController::class.java)

    @GetMapping
    fun summaryPage(model: Model): String {
        model.addAttribute("funds", vgblFundRepository.findAll())
        return "vgbl_summary"
    }

    @PostMapping
    fun generateSummary(
        model: Model,
        @RequestParam("cnpjs") cnpjs: String,
        @RequestParam("from") from: String,
        @RequestParam("to") to: String,
        @RequestParam("currency", defaultValue = "BRL") currency: String
    ): String {
        model.addAttribute("funds", vgblFundRepository.findAll())

        val cnpjsList = cnpjs.split(",").map { it.trim() }
        val normalizedFrom = from.replace("-", "")
        val normalizedTo = to.replace("-", "")

        val summaries = cnpjsList.mapNotNull { cnpj ->
            val fund = vgblFundRepository.findById(cnpj).orElse(null) ?: return@mapNotNull null
            log.info("Fetching income data for ${fund.fundName} for the period from $normalizedFrom to $normalizedTo")

            val incomeData = vgblFundService.getIncomeDataForPeriod(
                cnpj = cnpj,
                start = normalizedFrom.firstDayFromYearMonth(),
                end = normalizedTo.firstDayFromYearMonth(),
                currency = currency
            )

            val totalIncome = incomeData
                .map { it.incomeDifference }
                .fold(BigDecimal.ZERO) { acc, value -> acc.add(value ?: BigDecimal.ZERO) }
                .setScale(2, RoundingMode.HALF_EVEN)

            FundSummary(
                fundName = fund.fundName,
                cnpj = cnpj,
                totalIncome = totalIncome
            )
        }

        val grandTotal = summaries
            .map { it.totalIncome }
            .fold(BigDecimal.ZERO) { acc, value -> acc.add(value) }
            .setScale(2, RoundingMode.HALF_EVEN)

        model.addAttribute("summaries", summaries)
        model.addAttribute("grandTotal", grandTotal)
        model.addAttribute("from", from)
        model.addAttribute("to", to)
        model.addAttribute("currency", currency)
        model.addAttribute("selectedCnpjs", cnpjsList)

        return "vgbl_summary"
    }

    data class FundSummary(
        val fundName: String,
        val cnpj: String,
        val totalIncome: BigDecimal
    )
}
