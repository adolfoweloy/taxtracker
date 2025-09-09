package com.adolfoeloy.taxtracker.util

import java.time.LocalDate

fun LocalDate.toYearMonthString(): String {
    val month = this.monthValue.toString().padStart(2, '0')
    return "${this.year}$month"
}
