package com.adolfoeloy.taxtracker.vgbl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class VGBLTrackServiceTest {

    @Mock
    private lateinit var vgblTrackRepositoryMock: VGBLTrackRepository

    private lateinit var subject: VGBLTrackService

    private lateinit var fund: VGBLFund

    @BeforeEach
    fun setup() {
        subject = VGBLTrackService(vgblTrackRepositoryMock)
        fund = bradescoTruxtMacro()
    }

    @Test
    fun `should get zero quotas when fetching quotas after full redemption`() {
        // arrange
        whenever(vgblTrackRepositoryMock.findByVgblFund(fund)).thenReturn(vgblTrackList(fund))

        // act
        val quotas = subject.getAmountOfQuotasAt(fund, LocalDate.of(2026, 10, 1))

        // assert
        assertThat(quotas).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun `should get quotas greater than zero when fetching quotas before full redemption`() {
        // arrange
        whenever(vgblTrackRepositoryMock.findByVgblFund(fund)).thenReturn(vgblTrackList(fund))

        // act
        val quotas = subject.getAmountOfQuotasAt(fund, LocalDate.of(2024, 10, 1))

        // assert
        assertThat(quotas).isEqualTo(BigDecimal.TEN)
    }

    @Test
    fun `should get zero quotas when fund has no quotas`() {
        // arrange
        whenever(vgblTrackRepositoryMock.findByVgblFund(fund)).thenReturn(emptyList<VGBLTrack>())

        // act
        val quotas = subject.getAmountOfQuotasAt(fund, LocalDate.of(2024, 10, 1))

        // assert
        assertThat(quotas).isEqualTo(BigDecimal.ZERO)
    }

    companion object {
        fun bradescoTruxtMacro() =
            VGBLFund().apply {
                cnpj = "26.756.416/0001-28"
                quotas = BigDecimal.TEN
                fundName = "BRADESCO TRUXT MACRO FICFIM PGBL/VGBL"
                planName = "BRADESCO VGBL TRUXT MACRO"
            }

        fun vgblTrackList(
            fund: VGBLFund
        ): List<VGBLTrack> = listOf(
            contribution(fund, BigDecimal.TEN, LocalDate.of(2022, 10, 1), BigDecimal.valueOf(20_000)),
            redemption(fund, BigDecimal.TEN, LocalDate.of(2025, 10, 1), BigDecimal.valueOf(25_000))
        )

        fun contribution(
            fund: VGBLFund,
            trackQuotas: BigDecimal,
            trackTransactionDate: LocalDate,
            trackAmount: BigDecimal
        ): VGBLTrack =
            track(fund, trackQuotas, trackTransactionDate, trackAmount, VGBLTransactionType.CONTRIBUTION)

        fun redemption(
            fund: VGBLFund,
            trackQuotas: BigDecimal,
            trackTransactionDate: LocalDate,
            trackAmount: BigDecimal
        ): VGBLTrack =
            track(fund, trackQuotas, trackTransactionDate, trackAmount, VGBLTransactionType.REDEMPTION)

        private fun track(
            fund: VGBLFund,
            trackQuotas: BigDecimal,
            trackTransactionDate: LocalDate,
            trackAmount: BigDecimal,
            trackTransactionType: VGBLTransactionType
        ): VGBLTrack = VGBLTrack().apply {
            vgblFund = fund
            quotas = trackQuotas
            transactionDate = trackTransactionDate
            amount = trackAmount
            transactionType = trackTransactionType
        }
    }
}