package com.adolfoeloy.taxtracker

import com.adolfoeloy.taxtracker.fixture.FundMother
import com.adolfoeloy.taxtracker.fixture.VGBLTrackMother
import com.adolfoeloy.taxtracker.vgbl.VGBLFundRepository
import com.adolfoeloy.taxtracker.vgbl.VGBLTrackRepository
import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import kotlin.test.Test

class VGBLTrackRepositoryIT : AbstractDatabaseIntegrationTest() {

    @Autowired
    private lateinit var vgblTrackRepository: VGBLTrackRepository

    @Autowired
    private lateinit var vgblFundRepository: VGBLFundRepository

    @Test
    fun `should find vgbl track for a given fund`() {

        // Given: A VGBL fund with specific quotas
        val fundCnpj = "98.765.432/0001-09"
        val fundQuotas = "1000.000000000000".toBigDecimal()
        val fund = FundMother.createFund(fundCnpj, fundQuotas)
        val savedFund = vgblFundRepository.save(fund)

        // And there is two contributions for that VGBL fund
        val firstContribution = VGBLTrackMother.createContribution(
            vgblFund = savedFund,
            quotas = "500.000000000000".toBigDecimal(),
            amount = "20000.00".toBigDecimal(),
            quotaPrice = "1.234567890123".toBigDecimal(),
        )
        val secondContribution = VGBLTrackMother.createContribution(
            vgblFund = savedFund,
            quotas = "500.000000000000".toBigDecimal(),
            amount = "10000.00".toBigDecimal(),
            quotaPrice = "1.308531400000".toBigDecimal(),
            transactionDate = LocalDate.now().plusDays(1)
        )
        vgblTrackRepository.save(firstContribution)
        vgblTrackRepository.save(secondContribution)

        // When: Querying the track history for that fund
        val trackHistory = vgblTrackRepository.findByVgblFund(fund)

        // Then: should return two history entries for the selected fund
        Assertions.assertThat(trackHistory.size).isEqualTo(2)
    }
}