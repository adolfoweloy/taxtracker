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

}