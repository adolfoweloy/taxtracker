package com.adolfoeloy.taxtracker.vgbl.v2

import com.adolfoeloy.taxtracker.vgbl.FundHolding
import com.adolfoeloy.taxtracker.vgbl.VGBLHoldingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import java.math.BigDecimal
import java.time.LocalDate

@WebMvcTest(InvestmentFundDailyInfoPageController::class)
class InvestmentFundDailyInfoPageControllerIT {

    @Autowired
    lateinit var mvc: MockMvcTester

    @MockitoBean
    lateinit var vgblHoldingServiceMock: VGBLHoldingService

    @MockitoBean
    lateinit var importServiceMock: InvestmentFundDailyImportService

    @MockitoBean
    lateinit var repositoryMock: InvestmentFundDailyInfoRepository

    @BeforeEach
    fun setUp() {
        whenever(vgblHoldingServiceMock.listFundsWithHoldings(any()))
            .thenReturn(listOf(FundHolding(CNPJ, "Fund A", BigDecimal("1000"))))
    }

    @Test
    fun `should list the stored quotas of a fund for the searched month`() {
        whenever(
            repositoryMock.findByIdCnpjAndIdCompetenceDateBetweenOrderByIdCompetenceDate(
                CNPJ, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29)
            )
        ).thenReturn(
            listOf(
                dailyInfo(LocalDate.of(2024, 2, 1), "1.020000000000"),
                dailyInfo(LocalDate.of(2024, 2, 2), "1.025000000000")
            )
        )

        val result = mvc.get()
            .uri("/import/inf-diario-fi?searchCnpj={cnpj}&searchMonth=2024-02", CNPJ)
            .exchange()

        assertThat(result)
            .hasStatusOk()
            .model()
            .containsEntry(
                "quotas",
                listOf(
                    InvestmentFundDailyInfoPageController.QuotaView("01/02/2024", "1.02"),
                    InvestmentFundDailyInfoPageController.QuotaView("02/02/2024", "1.025")
                )
            )
    }

    @Test
    fun `should not search when the month is invalid`() {
        val result = mvc.get()
            .uri("/import/inf-diario-fi?searchCnpj={cnpj}&searchMonth=2024-13", CNPJ)
            .exchange()

        assertThat(result)
            .hasStatusOk()
            .model()
            .containsKey("searchError")
            .doesNotContainKey("quotas")
        verify(repositoryMock, never())
            .findByIdCnpjAndIdCompetenceDateBetweenOrderByIdCompetenceDate(any(), any(), any())
    }

    @Test
    fun `should not search when no month is given`() {
        val result = mvc.get()
            .uri("/import/inf-diario-fi")
            .exchange()

        assertThat(result)
            .hasStatusOk()
            .model()
            .doesNotContainKey("quotas")
    }

    private fun dailyInfo(date: LocalDate, quotaValue: String) = InvestmentFundDailyInfo().apply {
        id = InvestmentFundDailyInfoId(CNPJ, date)
        this.quotaValue = BigDecimal(quotaValue)
    }

    private companion object {
        const val CNPJ = "00.000.000/0001-00"
    }
}
