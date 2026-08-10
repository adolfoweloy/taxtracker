package com.adolfoeloy.taxtracker

import com.adolfoeloy.taxtracker.fixture.FundMother
import com.adolfoeloy.taxtracker.fixture.VGBLQuotaMother
import com.adolfoeloy.taxtracker.util.fromYYYYMMDDToLocalDate
import com.adolfoeloy.taxtracker.util.fromYearMonthString
import com.adolfoeloy.taxtracker.vgbl.VGBLFund
import com.adolfoeloy.taxtracker.vgbl.VGBLFundRepository
import com.adolfoeloy.taxtracker.vgbl.VGBLQuotaRepository
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

class VGBLFundRepositoryIT : AbstractDatabaseIntegrationTest() {

    @Autowired
    private lateinit var vgblFundRepository: VGBLFundRepository

    @Autowired
    private lateinit var vgblQuotaRepository: VGBLQuotaRepository

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

    private fun createQuota(
        quotaBuilder: VGBLQuotaMother.VGBLFundQuotaBuilder,
        quotaValue: String,
        competenceDate: String
    ) =
        quotaBuilder.createQuota(quotaValue.toBigDecimal(), competenceDate.fromYYYYMMDDToLocalDate())
}