package com.adolfoeloy.taxtracker.vgbl.v2

import com.adolfoeloy.taxtracker.vgbl.CsvCvmFundData
import com.adolfoeloy.taxtracker.vgbl.VGBLFund
import com.adolfoeloy.taxtracker.vgbl.VGBLFundRepository
import com.adolfoeloy.taxtracker.vgbl.VGBLTrackService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.math.BigDecimal
import java.time.LocalDate

@Controller
@RequestMapping("/import/inf-diario-fi")
class InvestmentFundDailyInfoPageController(
    private val vgblFundRepository: VGBLFundRepository,
    private val vgblTrackService: VGBLTrackService,
    private val investmentFundDailyInfoRepository: InvestmentFundDailyInfoRepository,
    private val csvCvmFundData: CsvCvmFundData,
) {
    private val log = LoggerFactory.getLogger(InvestmentFundDailyInfoPageController::class.java)

    @GetMapping
    fun importPage(model: Model): String {
        val today = LocalDate.now()

        val funds = vgblFundRepository.findAll()
            .map { fund -> mapFundsPresentation(fund, vgblTrackService.getAmountOfQuotasAt(fund, today)) }
            .toList()

        model.addAttribute("funds", funds)

        return "inf_diario_fi_import"
    }

    private fun mapFundsPresentation(fund: VGBLFund, quotas: BigDecimal): FundsPresentation {
        log.info("Fund ${fund.fundName} has $quotas quotas.")

        return FundsPresentation(
            fund.cnpj,
            fund.fundName,
            quotas > BigDecimal.ZERO
        )
    }

    data class FundsPresentation(
        val cnpj: String,
        val fundName: String,
        val hasQuotas: Boolean
    )
}