package com.adolfoeloy.taxtracker.report

import com.adolfoeloy.taxtracker.fixture.ReportMother
import com.adolfoeloy.taxtracker.util.NumberUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.test.web.servlet.assertj.MvcTestResult

@WebMvcTest(ReportController::class)
class ReportControllerIT {

    @TestConfiguration
    class NumberUtilsConfig {
        @Bean
        fun numberUtils() = NumberUtils()
    }

    @Autowired
    lateinit var mvc: MockMvcTester

    @MockitoBean
    lateinit var reportServiceMock: ReportService

    @Test
    fun `should return report page with model attributes for given month and year`() {
        val reportDataForJanuary2025 = ReportMother.createJanuaryReport()

        whenever(reportServiceMock.getReportData(1, 2025, "BRL"))
            .thenReturn(reportDataForJanuary2025)

        val result: MvcTestResult = mvc.get()
            .uri("/report/1/2025?currency=BRL")
            .exchange()

        assertThat(result)
            .hasStatusOk()
            .model()
                .containsKey("period")
                .containsKey("balanceBefore")
                .containsKey("balanceNow")
                .containsKey("transactions")
                .containsKey("totalGrossInterestEarned")
                .containsKey("currencyTicker")
    }

}