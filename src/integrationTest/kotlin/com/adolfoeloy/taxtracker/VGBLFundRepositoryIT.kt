package com.adolfoeloy.taxtracker

import com.adolfoeloy.taxtracker.fixture.FundMother
import com.adolfoeloy.taxtracker.fixture.VGBLQuotaMother
import com.adolfoeloy.taxtracker.fixture.VGBLTrackMother
import com.adolfoeloy.taxtracker.util.fromYYYYMMDDToLocalDate
import com.adolfoeloy.taxtracker.util.fromYearMonthString
import com.adolfoeloy.taxtracker.vgbl.VGBLFund
import com.adolfoeloy.taxtracker.vgbl.VGBLFundRepository
import com.adolfoeloy.taxtracker.vgbl.VGBLQuotaRepository
import com.adolfoeloy.taxtracker.vgbl.VGBLTrackRepository
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

class VGBLFundRepositoryIT : AbstractDatabaseIntegrationTest() {

    @Autowired
    private lateinit var vgblFundRepository: VGBLFundRepository

    @Autowired
    private lateinit var vgblQuotaRepository: VGBLQuotaRepository

    @Autowired
    private lateinit var vgblTrackRepository: VGBLTrackRepository

    @Test
    fun `should create a new VGBL fund correctly`() {
        val fundExample = FundMother.createFund(
            "12.345.678/0001-90",
            "1000.123456789012".toBigDecimal()
        )

        val result = vgblFundRepository
            .save<VGBLFund>(fundExample)

        val retrievedFund = vgblFundRepository.findById(result.cnpj)
        assertThat(retrievedFund.get())
            .usingRecursiveComparison()
            .isEqualTo(fundExample)
    }

    @Test
    fun `should find the incomes within a period for a given fund`() {
        // Given: A VGBL fund with specific quotas. A round, fictitious holding — the quota count
        // only scales the resulting balances, so nothing here depends on its value.
        val fundCnpj = "98.765.432/0001-09"
        val fundQuotas = "1000.000000000000".toBigDecimal()
        val fund = FundMother.createFund(fundCnpj, fundQuotas)
        vgblFundRepository.save(fund)

        // And: Monthly quota values from July to November. Each expected balance is simply
        // fundQuotas * the quota value below, and every assertion recomputes it.
        val quotaBuilder = VGBLQuotaMother.withVGBLFund(fund)
        val quotas = listOf(
            createQuota(quotaBuilder, "1.234567890123", "2024-07-01"),
            createQuota(quotaBuilder, "1.276458300000", "2024-07-31"),
            createQuota(quotaBuilder, "1.345678901234", "2024-08-01"),
            createQuota(quotaBuilder, "1.300695300000", "2024-08-29"),
            createQuota(quotaBuilder, "1.456789012345", "2024-09-04"),
            createQuota(quotaBuilder, "1.308531400000", "2024-09-30"),
            createQuota(quotaBuilder, "1.567890123456", "2024-10-01"),
            createQuota(quotaBuilder, "1.333202300000", "2024-10-31"),
            createQuota(quotaBuilder, "1.333937308600", "2024-11-03"),
            createQuota(quotaBuilder, "1.334469301700", "2024-11-06")
        )
        quotas.forEach { vgblQuotaRepository.save(it) }

        // When: Querying incomes for August to October period
        val result = vgblFundRepository.getIncomeDifferenceByCompetenceDate(
            cnpj = fundCnpj,
            startDate = "202407".fromYearMonthString(),
            endDate = "202410".fromYearMonthString()
        )

        // Then: Should return 3 months with calculated income differences
        assertThat(result).hasSize(3)

        // And: Each month should show income and difference from previous month
        val july = result[0]
        assertThat(july.competenceDate).isEqualTo("2024-07-31".fromYYYYMMDDToLocalDate())
        assertThat(july.income).isEqualTo(fundQuotas * "1.276458300000".toBigDecimal())
        assertThat(july.previousIncome).isNull() // no previous income for the first month

        val august = result[1] // this shows the income from July (calculated by subtracting 1st day of August - 1st day of July)
        assertThat(august.competenceDate).isEqualTo("2024-08-29".fromYYYYMMDDToLocalDate())
        assertThat(august.income).isEqualTo(fundQuotas * "1.300695300000".toBigDecimal())
        assertThat(august.income.minus(august.previousIncome!!)).isEqualTo(august.income - july.income)

        val september = result[2] // this shows the income from August (calculated by subtracting 1st day of September - 1st day of August)
        assertThat(september.competenceDate).isEqualTo("2024-09-30".fromYYYYMMDDToLocalDate())
        assertThat(september.income).isEqualTo(fundQuotas * "1.308531400000".toBigDecimal())
        assertThat(september.income.minus(september.previousIncome!!)).isEqualTo(september.income - august.income)

    }

    @Test
    fun `should use the quota count held as of each competence date when a partial contribution happens mid-period`() {
        // Given: A fund whose current total (1500) was reached via two contributions -
        // 1000 quotas before the reporting window, and another 500 mid-window.
        val fundCnpj = "11.222.333/0001-44"
        val fund = FundMother.createFund(fundCnpj, "1500.000000000000".toBigDecimal())
        vgblFundRepository.save(fund)

        vgblTrackRepository.save(
            VGBLTrackMother.createContribution(
                vgblFund = fund,
                quotas = "1000.000000000000".toBigDecimal(),
                amount = "1000.00".toBigDecimal(),
                quotaPrice = "1.000000000000".toBigDecimal(),
                transactionDate = "2024-06-01".fromYYYYMMDDToLocalDate()
            )
        )
        vgblTrackRepository.save(
            VGBLTrackMother.createContribution(
                vgblFund = fund,
                quotas = "500.000000000000".toBigDecimal(),
                amount = "500.00".toBigDecimal(),
                quotaPrice = "1.300000000000".toBigDecimal(),
                transactionDate = "2024-08-15".fromYYYYMMDDToLocalDate()
            )
        )

        val quotaBuilder = VGBLQuotaMother.withVGBLFund(fund)
        val quotas = listOf(
            createQuota(quotaBuilder, "1.200000000000", "2024-07-01"),
            createQuota(quotaBuilder, "1.250000000000", "2024-07-31"),
            createQuota(quotaBuilder, "1.300000000000", "2024-08-01"),
            createQuota(quotaBuilder, "1.350000000000", "2024-08-29"),
            createQuota(quotaBuilder, "1.400000000000", "2024-09-04"),
            createQuota(quotaBuilder, "1.450000000000", "2024-09-30")
        )
        quotas.forEach { vgblQuotaRepository.save(it) }

        // When: Querying incomes for July to September
        val result = vgblFundRepository.getIncomeDifferenceByCompetenceDate(
            cnpj = fundCnpj,
            startDate = "202407".fromYearMonthString(),
            endDate = "202409".fromYearMonthString()
        )

        assertThat(result).hasSize(2)

        // Then: July income is priced with only the first contribution's 1000 quotas -
        // the second contribution (08-15) hadn't happened yet.
        val july = result[0]
        assertThat(july.competenceDate).isEqualTo("2024-07-31".fromYYYYMMDDToLocalDate())
        assertThat(july.income).isEqualTo("1000.000000000000".toBigDecimal() * "1.250000000000".toBigDecimal())

        // And: August income (competence 08-29) is priced with the full 1500 quotas -
        // the 08-15 contribution has already happened.
        val august = result[1]
        assertThat(august.competenceDate).isEqualTo("2024-08-29".fromYYYYMMDDToLocalDate())
        assertThat(august.income).isEqualTo("1500.000000000000".toBigDecimal() * "1.350000000000".toBigDecimal())
    }

    private fun createQuota(
        quotaBuilder: VGBLQuotaMother.VGBLFundQuotaBuilder,
        quotaValue: String,
        competenceDate: String
    ) =
        quotaBuilder.createQuota(quotaValue.toBigDecimal(), competenceDate.fromYYYYMMDDToLocalDate())
}