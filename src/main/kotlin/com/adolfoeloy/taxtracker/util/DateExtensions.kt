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

/**
 * Converts a string in the format "YYYY-MM-DD" to a LocalDate.
 * Example: "2024-06-15" -> LocalDate.of(2024, 6, 15)
 */
fun String.fromYYYYMMDDToLocalDate(): LocalDate {
    val parts = this.split("-")
    val year = parts[0].toInt()
    val month = parts[1].toInt()
    val day = parts[2].toInt()
    return LocalDate.of(year, month, day)
}

fun LocalDate.lastDay(): LocalDate {
    return this.withDayOfMonth(this.lengthOfMonth())
}