package com.adolfoeloy.taxtracker.vgbl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class VGBLHoldingServiceTest {

    @Mock
    private lateinit var vgblFundRepositoryMock: VGBLFundRepository

    @Mock
    private lateinit var vgblTrackServiceMock: VGBLTrackService

    private lateinit var subject: VGBLHoldingService

    private val date = LocalDate.of(2026, 1, 15)

    @BeforeEach
    fun setUp() {
        subject = VGBLHoldingService(vgblFundRepositoryMock, vgblTrackServiceMock)
    }

    @Test
    fun `should list each fund with the quotas held at the given date`() {
        val held = fund("26.756.416/0001-28", "TRUXT MACRO")
        val redeemed = fund("11.111.111/0001-11", "OTHER FUND")
        whenever(vgblFundRepositoryMock.findAll()).thenReturn(listOf(held, redeemed))
        whenever(vgblTrackServiceMock.getAmountOfQuotasAt(held, date)).thenReturn(BigDecimal.TEN)
        whenever(vgblTrackServiceMock.getAmountOfQuotasAt(redeemed, date)).thenReturn(BigDecimal.ZERO)

        val holdings = subject.listFundsWithHoldings(date)

        assertThat(holdings).containsExactly(
            FundHolding("26.756.416/0001-28", "TRUXT MACRO", BigDecimal.TEN),
            FundHolding("11.111.111/0001-11", "OTHER FUND", BigDecimal.ZERO)
        )
    }

    @Test
    fun `should list nothing when no fund is registered`() {
        whenever(vgblFundRepositoryMock.findAll()).thenReturn(emptyList())

        assertThat(subject.listFundsWithHoldings(date)).isEmpty()
    }

    private fun fund(cnpj: String, name: String) = VGBLFund().apply {
        this.cnpj = cnpj
        fundName = name
    }
}
