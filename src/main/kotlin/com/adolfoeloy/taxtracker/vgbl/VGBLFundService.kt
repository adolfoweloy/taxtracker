package com.adolfoeloy.taxtracker.vgbl

import com.adolfoeloy.taxtracker.forex.ForexService
import com.adolfoeloy.taxtracker.util.fromStringToBigDecimal
import com.adolfoeloy.taxtracker.util.fromYYYYMMDDToLocalDate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

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
        start: LocalDate,
        end: LocalDate,
        currency: String
    ): List<VGBLMonthIncome> {
        return vgblFundRepository.getIncomeDifferenceByCompetenceDate(
            cnpj = cnpj,
            startDate = previousMonth(start),
            endDate = end
        )
        // The anchor row exists only to give the first reported month something to subtract from,
        // and it is exactly the row whose LAG is null. Filtering on the null rather than on the
        // anchor month also covers a window whose anchor month has no quota row at all — that
        // case used to reach the arithmetic below with a null and blow up.
        .filter { it.previousIncome != null }
        .map {
            val rawPreviousIncome = it.previousIncome!!
            val income = if (currency != "BRL") forexService.applyForexRateFor(it.income, it.competenceDate, currency) else it.income
            val previousIncome = if (currency != "BRL") forexService.applyForexRateFor(rawPreviousIncome, it.competenceDate, currency) else rawPreviousIncome

            VGBLMonthIncome(
                competenceDate = it.competenceDate.toString(),
                income = income,
                previousIncome = previousIncome,
                incomeDifference = income.minus(previousIncome),
                fundsReturnPercent = if (previousIncome.signum() == 0) null else income
                    .minus(previousIncome)
                    .divide(previousIncome, 12, RoundingMode.HALF_EVEN)
                    .multiply(ONE_HUNDRED)
            )
        }
    }

    /**
     * Month-by-month percentage return for one fund across one Australian FY, mirroring the
     * "Rentabilidade do Fundo" block of the Bradesco statement.
     *
     * Always evaluated in BRL: income is `quotas × quota_value` with a constant `quotas`, so both
     * the quota count and any forex rate cancel out of the ratio. The percentages are identical
     * in every currency, which is why this report has no currency selector.
     */
    fun getFundPerformanceForFY(cnpj: String, financialYear: Int): FundPerformance? {
        val fund = vgblFundRepository.findById(cnpj).orElse(null) ?: return null

        val fyStart = financialYearStart(financialYear)
        val fyEndExclusive = financialYearStart(financialYear + 1)

        val months = getIncomeDataForPeriod(cnpj, fyStart, fyEndExclusive, "BRL")

        val monthPerformances = months.map {
            val competenceDate = it.competenceDate.fromYYYYMMDDToLocalDate()
            MonthPerformance(
                yearMonth = YearMonth.from(competenceDate),
                competenceDate = competenceDate,
                returnPercent = it.fundsReturnPercent?.setScale(2, RoundingMode.HALF_EVEN)
            )
        }

        val reportedMonths = monthPerformances.map { it.yearMonth }.toSet()
        val fyMonths = (0L until 12L).map { YearMonth.from(fyStart).plusMonths(it) }

        return FundPerformance(
            fundName = fund.fundName,
            cnpj = cnpj,
            financialYear = financialYear,
            months = monthPerformances,
            fyReturnPercent = compoundedReturnPercent(months),
            missingMonths = fyMonths.filterNot { reportedMonths.contains(it) }
        )
    }

    /**
     * The FY figure compounds — it is not the sum of the monthly percentages. Because `LAG` chains
     * contiguously through whatever rows exist, the product of (1 + monthly return) telescopes
     * exactly to `lastIncome / firstPreviousIncome`, so computing it that way is both equivalent
     * and free of the drift that folding twelve rounded percentages would accumulate.
     */
    private fun compoundedReturnPercent(months: List<VGBLMonthIncome>): BigDecimal? {
        val opening = months.firstOrNull()?.previousIncome ?: return null
        if (opening.signum() == 0) return null

        return months.last().income
            .divide(opening, 12, RoundingMode.HALF_EVEN)
            .minus(BigDecimal.ONE)
            .multiply(ONE_HUNDRED)
            .setScale(2, RoundingMode.HALF_EVEN)
    }

    /** FYs worth offering: those holding quota data, and preceded by an anchor row to measure from. */
    fun getAvailableFinancialYears(): List<Int> {
        val competenceDates = vgblQuotaRepository.findDistinctCompetenceDates()

        return competenceDates
            .map { financialYearOf(it) }
            .distinct()
            .sorted()
            .filter { financialYear -> competenceDates.any { it < financialYearStart(financialYear) } }
    }

    /** Australian FY runs July–June and is named for the year it ends in: FY26 is 2025-07 … 2026-06. */
    fun financialYearStart(financialYear: Int): LocalDate = LocalDate.of(2000 + financialYear - 1, 7, 1)

    fun financialYearOf(date: LocalDate): Int =
        if (date.monthValue >= 7) date.year + 1 - 2000 else date.year - 2000

    fun previousMonth(date: LocalDate): LocalDate =
        date.minusMonths(1).withDayOfMonth(1)

    private companion object {
        val ONE_HUNDRED: BigDecimal = BigDecimal("100")
    }
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
    /** Null when the previous balance is zero, which would otherwise be a division by zero. */
    val fundsReturnPercent: BigDecimal?
)

data class FundPerformance(
    val fundName: String,
    val cnpj: String,
    val financialYear: Int,
    val months: List<MonthPerformance>,
    val fyReturnPercent: BigDecimal?,
    val missingMonths: List<YearMonth>
) {
    /** Bradesco names the FY by both ends: FY26 renders as "2025-26". */
    val financialYearLabel: String get() = "${2000 + financialYear - 1}-$financialYear"

    /**
     * Four rows of three cells laid out Jul–Oct | Nov–Feb | Mar–Jun, matching the statement.
     *
     * Every one of the twelve FY months gets a slot whether or not data exists for it, so a gap
     * shows up as a visibly empty cell in the right place instead of silently shifting the
     * remaining months into the wrong columns.
     */
    val monthGrid: List<List<MonthSlot>>
        get() {
            val fyStart = YearMonth.of(2000 + financialYear - 1, 7)
            val slots = (0L until 12L).map { offset ->
                val yearMonth = fyStart.plusMonths(offset)
                MonthSlot(yearMonth, months.firstOrNull { it.yearMonth == yearMonth })
            }
            return (0 until 4).map { row -> (0 until 3).map { column -> slots[column * 4 + row] } }
        }
}

data class MonthSlot(
    val yearMonth: YearMonth,
    val performance: MonthPerformance?
) {
    /** Pinned to English regardless of server locale — the statement is Portuguese, this report isn't. */
    val label: String get() = yearMonth.format(MONTH_LABEL_FORMAT).uppercase(Locale.ENGLISH)

    private companion object {
        val MONTH_LABEL_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM/yyyy", Locale.ENGLISH)
    }
}

data class MonthPerformance(
    val yearMonth: YearMonth,
    /** The day the percentage was actually measured on — not necessarily the true month end. */
    val competenceDate: LocalDate,
    val returnPercent: BigDecimal?
)
