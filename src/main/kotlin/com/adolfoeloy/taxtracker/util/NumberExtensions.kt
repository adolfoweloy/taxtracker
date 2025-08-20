    package com.adolfoeloy.taxtracker.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * Converts an integer representing cents to a formatted currency string.
 * Example: 123456 -> "1,234.56"
 */
fun Int.fromCents(scale: Int = 2): String {
    val zeros = "0".repeat(scale)
    return fromCentsToBigDecimal(scale)
        .let { decimal ->
            val formatter = DecimalFormat("#,##0.${zeros}")
            formatter.format(decimal)
        }
}

fun Int.fromCentsToBigDecimal(scale: Int = 2): BigDecimal {
    return BigDecimal.valueOf(this.toLong())
        .movePointLeft(scale)
        .setScale(scale, RoundingMode.HALF_EVEN)
}

fun BigDecimal.toCents(scale: Int = 2): Int {
    return this.setScale(scale, RoundingMode.HALF_EVEN)
        .movePointRight(scale)
        .toInt()
}