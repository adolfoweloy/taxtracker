package com.adolfoeloy.taxtracker.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class NumberExtensionsTest {

    @Test
    fun `String to BigDecimal should work with a number with precision 27 and scale 12`() {
        val input = "2.118156760000"

        val result = input.fromStringToBigDecimal(scale = 12)

        assertThat(result).isEqualTo(BigDecimal("2.118156760000"))
    }

}