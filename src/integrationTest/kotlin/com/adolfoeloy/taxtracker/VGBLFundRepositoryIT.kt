package com.adolfoeloy.taxtracker

import com.adolfoeloy.taxtracker.fixture.FundMother
import com.adolfoeloy.taxtracker.fixture.VGBLQuotaMother
import com.adolfoeloy.taxtracker.util.fromYYYYMMDDToLocalDate
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
        // Given: A VGBL fund with specific quotas
        val fundCnpj = "98.765.432/0001-09"
        val fundQuotas = "18947.530971000000".toBigDecimal()
        val fund = FundMother.createFund(fundCnpj, fundQuotas)
        vgblFundRepository.save(fund)

        // And: Monthly quota values from July to November
        val quotaBuilder = VGBLQuotaMother.withVGBLFund(fund)
        val quotas = listOf(
            createQuota(quotaBuilder, "1.234567890123", "2024-07-01"),  // 23392.013333908
            createQuota(quotaBuilder, "1.276458300000", "2024-07-31"),  // 24185.733172440009
            createQuota(quotaBuilder, "1.345678901234", "2024-08-01"),  // 25497.292658152
            createQuota(quotaBuilder, "1.300695300000", "2024-08-29"),  // 24644.964480584136300000000000
            createQuota(quotaBuilder, "1.456789012345", "2024-09-04"),  // 27602.554929619
            createQuota(quotaBuilder, "1.308531400000", "2024-09-30"),  // 24793.439228025989400000000000
            createQuota(quotaBuilder, "1.567890123456", "2024-10-01"),  // 29707.646673308
            createQuota(quotaBuilder, "1.333202300000", "2024-10-31"),  // 25260.891869858433300000000000
            createQuota(quotaBuilder, "1.333937308600", "2024-11-03"),   // 31811.033139208
            createQuota(quotaBuilder, "1.334469301700", "2024-11-06")   // 25284.898423809492950700000000
        )
        quotas.forEach { vgblQuotaRepository.save(it) }

        // When: Querying incomes for August to October period
        val result = vgblFundRepository.getIncomeDifferenceByCompetenceDate(
            cnpj = fundCnpj,
            year = 2024,
            startMonth = 7,
            endMonth = 10
        )

        // Then: Should return 3 months with calculated income differences
        assertThat(result).hasSize(4)

        // And: Each month should show income and difference from previous month
        val july = result[0]
        assertThat(july.competenceDate).isEqualTo("2024-07-31".fromYYYYMMDDToLocalDate())
        assertThat(july.income).isEqualTo(fundQuotas * "1.276458300000".toBigDecimal())

        val august = result[1] // this shows the income from July (calculated by subtracting 1st day of August - 1st day of July)
        assertThat(august.competenceDate).isEqualTo("2024-08-29".fromYYYYMMDDToLocalDate())
        assertThat(august.income).isEqualTo(fundQuotas * "1.300695300000".toBigDecimal())
        assertThat(august.incomeDifference).isEqualTo(august.income - july.income)

        val september = result[2] // this shows the income from August (calculated by subtracting 1st day of September - 1st day of August)
        assertThat(september.competenceDate).isEqualTo("2024-09-30".fromYYYYMMDDToLocalDate())
        assertThat(september.income).isEqualTo(fundQuotas * "1.308531400000".toBigDecimal())
        assertThat(september.incomeDifference).isEqualTo(september.income - august.income)

        val october = result[3] // this shows the income from September (calculated by subtracting 1st day of October - 1st day of September)
        assertThat(october.competenceDate).isEqualTo("2024-10-31".fromYYYYMMDDToLocalDate())
        assertThat(october.income).isEqualTo(fundQuotas * "1.333202300000".toBigDecimal())
        assertThat(october.incomeDifference).isEqualTo(october.income - september.income)
    }

    private fun createQuota(
        quotaBuilder: VGBLQuotaMother.VGBLFundQuotaBuilder,
        quotaValue: String,
        competenceDate: String
    ) =
        quotaBuilder.createQuota(quotaValue.toBigDecimal(), competenceDate.fromYYYYMMDDToLocalDate())
}