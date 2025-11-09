package com.adolfoeloy.taxtracker.vgbl

import com.adolfoeloy.taxtracker.forex.ForexService
import com.adolfoeloy.taxtracker.util.fromStringToBigDecimal
import com.adolfoeloy.taxtracker.util.fromYYYYMMDDToLocalDate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

// the whole plan is, to import all data from CVM
// with data imported, create a service that will fetch the quotas and calculate the interest and taxes

@Component
class VGBLFundService(
    private val vgblQuotaRepository: VGBLQuotaRepository,
    private val vgblFundRepository: VGBLFundRepository,
    private val forexService: ForexService,
) {

    fun saveQuotaValue(
        cvmFundData: CvmFundData
    ): VGBLQuota {

        val vgblQuota = vgblQuotaRepository.findById(VGBLQuotaId(
            cnpj = cvmFundData.cnpj,
            competenceDate = cvmFundData.date.fromYYYYMMDDToLocalDate()
        ))

        if (vgblQuota.isPresent) {
            return vgblQuota.get()
        }

        val quota = VGBLQuota().apply {
            id.cnpj = cvmFundData.cnpj
            id.competenceDate = cvmFundData.date.fromYYYYMMDDToLocalDate()
            fundClass = cvmFundData.fundType
            quotaValue = cvmFundData.quotaValue.fromStringToBigDecimal(scale = 12)
        }

        vgblQuotaRepository.save(quota)

        return quota
    }

    fun saveFund(vgblFundService: VGBLFundRequest): VGBLFund {
        val fund = VGBLFund().apply {
            cnpj = vgblFundService.cnpj
            planName = vgblFundService.planName
            fundName = vgblFundService.fundName
            quotas = vgblFundService.quotas.fromStringToBigDecimal(scale = 12)
        }

        val savedFund = vgblFundRepository.save(fund)

        return savedFund
    }

    fun getIncomeDataForPeriod(
        cnpj: String,
        year: Int,
        startMonth: Int,
        endMonth: Int,
        currency: String
    ): List<VGBLMonthIncome> {
        return vgblFundRepository.getIncomeDifferenceByCompetenceDate(
            cnpj = cnpj,
            year = year,
            startMonth = baseMonth(startMonth),
            endMonth = endMonth
        ).filter { it.competenceDate.monthValue != baseMonth(startMonth) }
        .map {
            val income = if (currency != "BRL") forexService.applyForexRateFor(it.income, it.competenceDate, currency) else it.income
            val previousIncome = if (currency != "BRL") forexService.applyForexRateFor(it.previousIncome, it.competenceDate, currency) else it.previousIncome

            VGBLMonthIncome(
                competenceDate = it.competenceDate.toString(),
                income = income,
                previousIncome = previousIncome,
                incomeDifference = income.minus(previousIncome),
                fundsReturnPercent = income
                    .minus(previousIncome)
                    .divide(previousIncome, 12, RoundingMode.HALF_EVEN)
                    .multiply("100".toBigDecimal())
            )
        }
    }

     fun baseMonth(startMonth: Int): Int = if (startMonth == 1) 12 else startMonth - 1
}

data class VGBLFundRequest(
    val cnpj: String,
    val planName: String,
    val fundName: String,
    val quotas: String
)

data class VGBLMonthIncome(
    val competenceDate: String,
    val income: BigDecimal,
    val previousIncome: BigDecimal?,
    val incomeDifference: BigDecimal?,
    val fundsReturnPercent: BigDecimal
)
