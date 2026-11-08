package com.adolfoeloy.taxtracker.vgbl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class VGBLHoldingService(
    private val vgblFundRepository: VGBLFundRepository,
    private val vgblTrackService: VGBLTrackService
) {
    private val log = LoggerFactory.getLogger(VGBLHoldingService::class.java)

    fun listFundsWithHoldings(date: LocalDate): List<FundHolding> {
        return vgblFundRepository.findAll().map { fund ->
            val quotas = vgblTrackService.getAmountOfQuotasAt(fund, date)
            log.info("Fund ${fund.fundName} has $quotas quotas.")

            FundHolding(fund.cnpj, fund.fundName, quotas)
        }
    }
}
