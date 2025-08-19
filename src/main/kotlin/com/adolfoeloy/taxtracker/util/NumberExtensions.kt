    package com.adolfoeloy.taxtracker.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * Converts an integer representing cents to a formatted currency string.
 * Example: 123456 -> "1,234.56"
 */
fun Int.fromCents(): String {
    return BigDecimal.valueOf(this.toLong())
        .movePointLeft(2)
        .setScale(2, RoundingMode.HALF_UP)
        .let { decimal ->
            val formatter = DecimalFormat("#,##0.00")
            formatter.format(decimal)
        }
}
