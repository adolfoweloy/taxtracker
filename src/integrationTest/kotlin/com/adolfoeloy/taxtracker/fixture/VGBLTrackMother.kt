package com.adolfoeloy.taxtracker.fixture

import com.adolfoeloy.taxtracker.vgbl.VGBLFund
import com.adolfoeloy.taxtracker.vgbl.VGBLTrack
import com.adolfoeloy.taxtracker.vgbl.VGBLTransactionType
import java.math.BigDecimal
import java.time.LocalDate

class VGBLTrackMother {

    companion object {
        fun createContribution(
            vgblFund: VGBLFund,
            quotas: BigDecimal,
            amount: BigDecimal,
            quotaPrice: BigDecimal,
            transactionDate: LocalDate = LocalDate.now(),
        ): VGBLTrack {
            return VGBLTrack().apply {
                this.vgblFund = vgblFund
                this.quotas = quotas
                this.amount = amount
                this.quotaPrice = quotaPrice
                this.transactionDate = transactionDate
                this.transactionType = VGBLTransactionType.CONTRIBUTION
            }
        }
    }
}