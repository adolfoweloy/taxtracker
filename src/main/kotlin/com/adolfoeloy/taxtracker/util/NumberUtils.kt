package com.adolfoeloy.taxtracker.util

import org.springframework.stereotype.Component

@Component("numberUtils")
class NumberUtils {

    fun fromCents(cents: Int): String {
        return cents.fromCents()
    }

    fun sumFromCents(values: List<Int>): String {
        return values.sumOf { it }.fromCents()
    }

}
