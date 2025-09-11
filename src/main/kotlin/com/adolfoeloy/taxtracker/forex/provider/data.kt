package com.adolfoeloy.taxtracker.forex.provider

import java.math.BigDecimal
import java.math.RoundingMode

data class ForexRate(
    val ticker: String,
    val rate: Int = 1, // Default rate is 1 for BRL
    val scale: Int = 6 // Default scale is 6 (e.g., 1.000000 for BRL
) {

    fun getBigDecimalRate(): BigDecimal {
        return BigDecimal(rate)
            .setScale(scale, RoundingMode.HALF_EVEN)
            .movePointLeft(scale) // Assuming rate is in cents, convert to BRL
    }

    override fun toString(): String {
        return "$ticker: $rate"
    }
}

class ForexRateException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)