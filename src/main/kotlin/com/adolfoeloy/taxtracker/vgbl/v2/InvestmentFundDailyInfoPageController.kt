package com.adolfoeloy.taxtracker.vgbl.v2

import com.adolfoeloy.taxtracker.vgbl.FundHolding
import com.adolfoeloy.taxtracker.vgbl.VGBLHoldingService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Controller
@RequestMapping("/import/inf-diario-fi")
class InvestmentFundDailyInfoPageController(
    private val vgblHoldingService: VGBLHoldingService,
    private val investmentFundDailyImportService: InvestmentFundDailyImportService,
    private val investmentFundDailyInfoRepository: InvestmentFundDailyInfoRepository
) {

    @GetMapping
    fun importPage(
        @RequestParam("searchCnpj", required = false) searchCnpj: String?,
        @RequestParam("searchMonth", required = false) searchMonth: String?,
        model: Model
    ): String {
        // flashed by the previous import, so the same funds stay selected for the next one
        val selectedCnpjs = (model.asMap()["selectedCnpjs"] as? List<*>)?.filterIsInstance<String>()?.toSet()

        val funds = vgblHoldingService.listFundsWithHoldings(LocalDate.now())
            .map { mapFundsPresentation(it, selectedCnpjs) }

        model.addAttribute("funds", funds)
        model.addAttribute("searchCnpj", searchCnpj)
        model.addAttribute("searchMonth", searchMonth)

        if (!searchCnpj.isNullOrBlank() && !searchMonth.isNullOrBlank()) {
            val month = runCatching { YearMonth.parse(searchMonth) }.getOrNull()
            if (month == null) {
                model.addAttribute("searchError", "Invalid month: $searchMonth. Use the yyyy-mm format.")
            } else {
                model.addAttribute("quotas", searchQuotas(searchCnpj, month))
            }
        }

        return "inf_diario_fi_import"
    }

    private fun searchQuotas(cnpj: String, month: YearMonth): List<QuotaView> =
        investmentFundDailyInfoRepository
            .findByIdCnpjAndIdCompetenceDateBetweenOrderByIdCompetenceDate(cnpj, month.atDay(1), month.atEndOfMonth())
            .map {
                QuotaView(
                    date = it.id.competenceDate.format(dateFormatter),
                    quotaValue = it.quotaValue.stripTrailingZeros().toPlainString()
                )
            }

    @PostMapping
    fun doImport(
        @RequestParam("cnpjs") cnpjs: String,
        @RequestParam("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes
    ): String {
        val cnpjList = parseCnpjs(cnpjs)
        redirectAttributes.addFlashAttribute("selectedCnpjs", cnpjList)

        val importResult = try {
            file.csvInputStream().use { investmentFundDailyImportService.doImport(cnpjList, it) }
        } catch (e: IllegalArgumentException) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
            return "redirect:/import/inf-diario-fi"
        }

        redirectAttributes.addFlashAttribute("summary", mapImportSummary(cnpjList.size, importResult))

        return "redirect:/import/inf-diario-fi"
    }

    private fun parseCnpjs(cnpjs: String): List<String> = cnpjs
        .split(",")
        .map { it.trim() }

    private fun mapFundsPresentation(holding: FundHolding, selectedCnpjs: Set<String>?): FundsPresentation {
        return FundsPresentation(
            holding.cnpj,
            holding.fundName,
            selected = selectedCnpjs?.contains(holding.cnpj) ?: (holding.quotas > BigDecimal.ZERO)
        )
    }

    private fun mapImportSummary(fundsRequested: Int, result: ImportResult): ImportSummaryView {
        return ImportSummaryView(
            successful = result.errors.isEmpty(),
            errors = result.errors,
            fundsRequested = fundsRequested,
            fundsImported = result.imported.size,
            rowsImported = result.imported.values.sumOf { it.size },
            rowsReplaced = result.imported.values.sumOf { quotas -> quotas.count { it.replacedQuotaValue != null } },
            funds = result.imported.map { (fund, quotas) ->
                FundImportView(
                    cnpj = fund.cnpj,
                    fundName = fund.fundName,
                    rows = quotas.size,
                    replaced = quotas.count { it.replacedQuotaValue != null },
                    from = quotas.minOfOrNull { it.competenceDate }?.format(dateFormatter) ?: "-",
                    to = quotas.maxOfOrNull { it.competenceDate }?.format(dateFormatter) ?: "-"
                )
            }
        )
    }

    data class FundsPresentation(
        val cnpj: String,
        val fundName: String,
        val selected: Boolean
    )

    data class ImportSummaryView(
        val successful: Boolean,
        val errors: List<String>,
        val fundsRequested: Int,
        val fundsImported: Int,
        val rowsImported: Int,
        /** Rows that already held a different quota value, so corrections are visible. */
        val rowsReplaced: Int,
        val funds: List<FundImportView>
    )

    data class QuotaView(
        val date: String,
        val quotaValue: String
    )

    data class FundImportView(
        val cnpj: String,
        val fundName: String,
        val rows: Int,
        val replaced: Int,
        val from: String,
        val to: String
    )

    private companion object {
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }
}
