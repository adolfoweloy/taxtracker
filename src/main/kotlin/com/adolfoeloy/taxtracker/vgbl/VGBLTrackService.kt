package com.adolfoeloy.taxtracker.vgbl

import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
class VGBLTrackService(
    private val vgblTrackRepository: VGBLTrackRepository
) {

    fun getAmountOfQuotasAt(vgblFund: VGBLFund, date: LocalDate): BigDecimal {
        val track = vgblTrackRepository.findByVgblFund(vgblFund)

        if (track.isEmpty()) {
            return BigDecimal.ZERO
        }

        return track
            .filter { track -> track.transactionDate <= date }
            .map { track -> track.quotas }
            .reduce { acc, quotas -> acc.plus(quotas) }
    }

}