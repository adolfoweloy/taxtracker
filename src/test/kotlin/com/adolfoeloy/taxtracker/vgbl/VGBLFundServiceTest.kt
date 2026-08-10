package com.adolfoeloy.taxtracker.vgbl

import com.adolfoeloy.taxtracker.forex.ForexService
import com.adolfoeloy.taxtracker.util.fromStringToBigDecimal
import com.adolfoeloy.taxtracker.util.fromYYYYMMDDToLocalDate
import com.adolfoeloy.taxtracker.util.fromYearMonthString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class VGBLFundServiceTest {

    @Mock
    private lateinit var vgblQuotaRepositoryMock: VGBLQuotaRepository

    @Mock // TODO: having to add another repository to this service is a smell that this service is doing too much
    private lateinit var vgblFundRepositoryMock: VGBLFundRepository

    @Mock
    private lateinit var forexServiceMock: ForexService

    private lateinit var subject: VGBLFundService

    @BeforeEach
    fun setUp() {
        subject = VGBLFundService(
            vgblQuotaRepository = vgblQuotaRepositoryMock,
            vgblFundRepository = vgblFundRepositoryMock,
            forexService = forexServiceMock
        )
    }

    @Test
    fun `the fund should be saved as the correct VGBL given the selected CvmFundData`() {
        val argumentCaptor = argumentCaptor<VGBLQuota>()

        val input = CvmFundData(
            fundType = "CLASSES - FIF",
            cnpj = "26.756.416/0001-28",
            subclassId = "",
            date = "2025-08-01",
            totalValue = "42637166.56",
            quotaValue = "1.279672500000",
            netAssetValue = "42645077.45",
            dailyCaptation = "601.80",
            dailyRedemption = "0.00",
            numberOfShareholders = "1"
        )

        subject.saveQuotaValue(input)
        verify(vgblQuotaRepositoryMock).save(argumentCaptor.capture())

        val expected = VGBLQuota().apply {
            id.cnpj = "26.756.416/0001-28"
            id.competenceDate = "2025-08-01".fromYYYYMMDDToLocalDate()
            fundClass = "CLASSES - FIF"
            quotaValue = "1.279672500000".fromStringToBigDecimal(scale = 12)
        }

        assertThat(argumentCaptor.firstValue)
            .usingRecursiveComparison()
            .isEqualTo(expected)
    }

    @Test
    fun `base month should return December when end month is January`() {
        val result = subject.previousMonth("202401".fromYearMonthString())
        assertThat(result).isEqualTo("202312".fromYearMonthString())
    }

    @Test
    fun `should report the monthly returns published on the Bradesco statement`() {
        givenFund(TRUXT_CNPJ)
        givenQuotaValues(TRUXT_CNPJ, TRUXT_FY2025_QUOTA_VALUES)

        val result = subject.getFundPerformanceForFY(TRUXT_CNPJ, 25)!!

        assertThat(result.months.map { it.returnPercent.toString() }).containsExactly(
            "1.37", "-0.59", "2.58", "2.69", "3.05", "4.39",
            "-2.71", "1.99", "-3.51", "2.64", "-2.15", "1.48"
        )
        assertThat(result.missingMonths).isEmpty()
    }

    @Test
    fun `FY return should compound the monthly returns rather than sum them`() {
        givenFund(TRUXT_CNPJ)
        givenQuotaValues(TRUXT_CNPJ, TRUXT_FY2025_QUOTA_VALUES)

        val result = subject.getFundPerformanceForFY(TRUXT_CNPJ, 25)!!

        // Compounded across the twelve months. Summing the same figures gives 11.23, so this
        // assertion is what stops the FY total from silently becoming an addition.
        assertThat(result.fyReturnPercent).isEqualTo("11.44".toBigDecimal())

        val summed = result.months.mapNotNull { it.returnPercent }.reduce(BigDecimal::add)
        assertThat(result.fyReturnPercent).isNotEqualTo(summed)
    }

    @Test
    fun `percentages should be identical in BRL and AUD`() {
        givenQuotaValues(TRUXT_CNPJ, TRUXT_FY2025_QUOTA_VALUES)

        // quotas and the forex rate both cancel out of the ratio, so an AUD run of the same data
        // must produce the same percentages. This locks in the "no currency selector" decision.
        whenever(forexServiceMock.applyForexRateFor(any<BigDecimal>(), any(), eq("AUD")))
            .thenAnswer { it.getArgument<BigDecimal>(0).multiply("0.271244".toBigDecimal()) }

        val brl = subject.getIncomeDataForPeriod(TRUXT_CNPJ, FY2025_START, FY2026_START, "BRL")
        val aud = subject.getIncomeDataForPeriod(TRUXT_CNPJ, FY2025_START, FY2026_START, "AUD")

        assertThat(aud.map { it.fundsReturnPercent!!.setScale(6, RoundingMode.HALF_EVEN) })
            .isEqualTo(brl.map { it.fundsReturnPercent!!.setScale(6, RoundingMode.HALF_EVEN) })
    }

    @Test
    fun `should drop the anchor row instead of throwing when it has no previous income`() {
        givenFund(TRUXT_CNPJ)
        // No anchor row before July, so the first row's LAG is null.
        givenIncomeDifferences(
            TRUXT_CNPJ,
            incomeDifference("2024-07-31", income = "100.00", previousIncome = null),
            incomeDifference("2024-08-30", income = "110.00", previousIncome = "100.00")
        )

        val result = subject.getFundPerformanceForFY(TRUXT_CNPJ, 25)!!

        assertThat(result.months).hasSize(1)
        assertThat(result.months.single().returnPercent).isEqualTo("10.00".toBigDecimal())
    }

    @Test
    fun `should not throw when the previous balance is zero`() {
        givenFund(TRUXT_CNPJ)
        givenIncomeDifferences(
            TRUXT_CNPJ,
            incomeDifference("2024-07-31", income = "100.00", previousIncome = "0.00")
        )

        val result = subject.getFundPerformanceForFY(TRUXT_CNPJ, 25)!!

        assertThat(result.months.single().returnPercent).isNull()
        assertThat(result.fyReturnPercent).isNull()
    }

    @Test
    fun `should flag FY months that have no stored quota value`() {
        givenFund(TRUXT_CNPJ)
        givenIncomeDifferences(
            TRUXT_CNPJ,
            incomeDifference("2025-07-31", income = "110.00", previousIncome = "100.00"),
            incomeDifference("2025-08-29", income = "121.00", previousIncome = "110.00")
        )

        val result = subject.getFundPerformanceForFY(TRUXT_CNPJ, 26)!!

        assertThat(result.months).hasSize(2)
        assertThat(result.missingMonths).containsExactly(
            YearMonth.of(2025, 9), YearMonth.of(2025, 10), YearMonth.of(2025, 11),
            YearMonth.of(2025, 12), YearMonth.of(2026, 1), YearMonth.of(2026, 2),
            YearMonth.of(2026, 3), YearMonth.of(2026, 4), YearMonth.of(2026, 5),
            YearMonth.of(2026, 6)
        )
    }

    @Test
    fun `should query the FY window from July to the following July exclusive`() {
        givenFund(TRUXT_CNPJ)
        givenIncomeDifferences(TRUXT_CNPJ)

        subject.getFundPerformanceForFY(TRUXT_CNPJ, 26)

        // The service widens the start by one month to fetch the anchor row.
        verify(vgblFundRepositoryMock).getIncomeDifferenceByCompetenceDate(
            cnpj = TRUXT_CNPJ,
            startDate = LocalDate.of(2025, 6, 1),
            endDate = LocalDate.of(2026, 7, 1)
        )
    }

    @Test
    fun `should return null for a CNPJ that is not registered`() {
        whenever(vgblFundRepositoryMock.findById("00.000.000/0001-00")).thenReturn(Optional.empty())

        assertThat(subject.getFundPerformanceForFY("00.000.000/0001-00", 25)).isNull()
    }

    @Test
    fun `available financial years should exclude a FY with no anchor row before it`() {
        // 2024-06-28 is the anchor for FY25; on its own it cannot produce a FY24 report.
        whenever(vgblQuotaRepositoryMock.findDistinctCompetenceDates()).thenReturn(
            listOf(
                LocalDate.of(2024, 6, 28),
                LocalDate.of(2024, 7, 31),
                LocalDate.of(2025, 6, 30),
                LocalDate.of(2025, 7, 31),
                LocalDate.of(2026, 6, 30)
            )
        )

        assertThat(subject.getAvailableFinancialYears()).containsExactly(25, 26)
    }

    private fun givenFund(cnpj: String) {
        val fund = VGBLFund().apply {
            this.cnpj = cnpj
            fundName = "BRADESCO TRUXT MACRO FICFIM PGBL/VGBL"
            planName = "BRADESCO VGBL TRUXT MACRO"
            quotas = QUOTAS
        }
        whenever(vgblFundRepositoryMock.findById(cnpj)).thenReturn(Optional.of(fund))
    }

    /** Turns month-end quota values into the LAG projection the native query would return. */
    private fun givenQuotaValues(cnpj: String, quotaValues: List<Pair<String, String>>) {
        val rows = quotaValues.mapIndexed { index, (date, quotaValue) ->
            incomeDifference(
                competenceDate = date,
                income = (quotaValue.toBigDecimal() * QUOTAS).toString(),
                previousIncome = quotaValues.getOrNull(index - 1)
                    ?.let { (_, previous) -> (previous.toBigDecimal() * QUOTAS).toString() }
            )
        }
        givenIncomeDifferences(cnpj, *rows.toTypedArray())
    }

    private fun givenIncomeDifferences(cnpj: String, vararg rows: IncomeDifference) {
        whenever(
            vgblFundRepositoryMock.getIncomeDifferenceByCompetenceDate(
                cnpj = eq(cnpj), startDate = any(), endDate = any()
            )
        ).thenReturn(rows.toList())
    }

    private fun incomeDifference(competenceDate: String, income: String, previousIncome: String?) =
        object : IncomeDifference {
            override val competenceDate: LocalDate = competenceDate.fromYYYYMMDDToLocalDate()
            override val income: BigDecimal = income.toBigDecimal()
            override val previousIncome: BigDecimal? = previousIncome?.toBigDecimal()
        }

    private companion object {
        const val TRUXT_CNPJ = "26.756.416/0001-28"
        val QUOTAS: BigDecimal = "18947.530971000000".toBigDecimal()
        val FY2025_START: LocalDate = LocalDate.of(2024, 7, 1)
        val FY2026_START: LocalDate = LocalDate.of(2025, 7, 1)

        /**
         * Month-end quota values for BRADESCO TRUXT MACRO, FY2025, as stored in the production
         * database. The June 2024 row is the anchor. Ten of the resulting twelve percentages are
         * printed on the Bradesco statement dated 09/09/2025 and agree exactly.
         */
        val TRUXT_FY2025_QUOTA_VALUES = listOf(
            "2024-06-28" to "1.148078300000",
            "2024-07-31" to "1.163848500000",
            "2024-08-30" to "1.156978600000",
            "2024-09-30" to "1.186886100000",
            "2024-10-31" to "1.218855700000",
            "2024-11-29" to "1.256015300000",
            "2024-12-31" to "1.311107300000",
            "2025-01-31" to "1.275514300000",
            "2025-02-28" to "1.300880900000",
            "2025-03-31" to "1.255257400000",
            "2025-04-30" to "1.288398900000",
            "2025-05-30" to "1.260675200000",
            "2025-06-30" to "1.279382600000"
        )
    }
}