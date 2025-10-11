package com.adolfoeloy.taxtracker.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DateExtensionsTest {

    @Test
    fun `toYearMonthString should return correct format`() {
        val date = LocalDate.of(2023, 3, 15)
        val result = date.toYearMonthString()
        assertEquals("202303", result)
    }

    @Test
    fun `toYearMonthString should pad month with leading zero`() {
        val date = LocalDate.of(2023, 1, 5)
        val result = date.toYearMonthString()
        assertEquals("202301", result)
    }

    @Test
    fun `fromYearMonthString should return correct LocalDate`() {
        val dateString = "202312"
        val result = dateString.fromYearMonthString()
        assertEquals(LocalDate.of(2023, 12, 1), result)
    }

    @Test
    fun `fromYearMonthString should handle leading zero in month`() {
        val dateString = "202301"
        val result = dateString.fromYearMonthString()
        assertEquals(LocalDate.of(2023, 1, 1), result)
    }

    @Test
    fun `lastDay should return the last day of the month`() {
        val date = LocalDate.of(2023, 2, 10)
        val result = date.lastDay()
        assertEquals(LocalDate.of(2023, 2, 28), result)
    }
}