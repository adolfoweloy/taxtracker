package com.adolfoeloy.taxtracker.fixture

import com.adolfoeloy.taxtracker.vgbl.VGBLFund
import com.adolfoeloy.taxtracker.vgbl.VGBLQuota
import java.math.BigDecimal
import java.time.LocalDate

class VGBLQuotaMother {

    companion object {
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