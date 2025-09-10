package com.adolfoeloy.taxtracker.forex.provider

import java.math.BigDecimal
import java.math.RoundingMode

data class ForexRate(
    val ticker: String,
    val rate: Int = 1, // Default rate is 1 for BRL
) {

    fun getBigDecimalRate(): BigDecimal {
        return BigDecimal(rate)
            .setScale(6, RoundingMode.HALF_EVEN)
            .movePointLeft(6) // Assuming rate is in cents, convert to BRL
    }

    override fun toString(): String {
        return "$ticker: $rate"
    }
}