package com.adolfoeloy.taxtracker.util

import java.time.LocalDate

fun LocalDate.toYearMonthString(): String {
    val month = this.monthValue.toString().padStart(2, '0')
    return "${this.year}$month"
}

fun String.fromYearMonthString(): LocalDate {
    val year = this.substring(0, 4).toInt()
    val month = this.substring(4, 6).toInt()
    return LocalDate.of(year, month, 1)
}

fun LocalDate.lastDay(): LocalDate {
    return this.withDayOfMonth(this.lengthOfMonth())
}