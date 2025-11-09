package com.adolfoeloy.taxtracker.fixture

import com.adolfoeloy.taxtracker.vgbl.VGBLFund
import com.adolfoeloy.taxtracker.vgbl.VGBLQuota
import java.math.BigDecimal
import java.time.LocalDate

class VGBLQuotaMother {

    companion object {
        val COMPETENCE_JULY: LocalDate = LocalDate.of(2024, 7, 1)
        val COMPETENCE_AUGUST: LocalDate = LocalDate.of(2024, 8, 1)
        val COMPETENCE_SEPTEMBER: LocalDate = LocalDate.of(2024, 9, 4)
        val COMPETENCE_OCTOBER: LocalDate = LocalDate.of(2024, 10, 1)
        val COMPETENCE_NOVEMBER: LocalDate = LocalDate.of(2024, 11, 3)

        fun withVGBLFund(vgblFund: VGBLFund) =
            VGBLFundQuotaBuilder(vgblFund)
    }

    class VGBLFundQuotaBuilder(val vgblFund: VGBLFund) {

        fun createQuota(quotaValue: BigDecimal, competenceDate: LocalDate) = VGBLQuota().apply {
            id.cnpj = vgblFund.cnpj
            id.competenceDate = competenceDate
            fundClass = "Fundo example class"
            this.quotaValue = quotaValue
        }
    }
}