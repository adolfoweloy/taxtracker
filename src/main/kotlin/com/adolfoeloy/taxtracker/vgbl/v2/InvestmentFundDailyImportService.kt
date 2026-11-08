package com.adolfoeloy.taxtracker.vgbl.v2

import com.adolfoeloy.taxtracker.util.fromStringToBigDecimal
import com.adolfoeloy.taxtracker.util.fromYYYYMMDDToLocalDate
import com.adolfoeloy.taxtracker.vgbl.CsvCvmFundData
import com.adolfoeloy.taxtracker.vgbl.CvmFundData
import com.adolfoeloy.taxtracker.vgbl.FundSummary
import com.adolfoeloy.taxtracker.vgbl.VGBLFundService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.io.InputStream
import java.math.BigDecimal
import java.time.LocalDate

@Component
class InvestmentFundDailyImportService(
    private val vgblFundService: VGBLFundService,
    private val csvCvmFundData: CsvCvmFundData,
    private val investmentFundDailyInfoRepository: InvestmentFundDailyInfoRepository
) {

    @Transactional
    fun doImport(
        cnpjList: List<String>,
        file: InputStream,
    ): ImportResult {
        val funds = cnpjList.mapNotNull(vgblFundService::findFundBy)

        val unregistered = cnpjList - funds.map { it.cnpj }.toSet()
        if (unregistered.isNotEmpty()) {
            return ImportResult(
                imported = emptyMap(),
                errors = listOf("Funds do not exist: ${unregistered.joinToString()}")
            )
        }

        val cvmFundData = csvCvmFundData.loadAllFrom(funds.map { it.cnpj }, file)
        val missing = funds.filterNot { it.cnpj in cvmFundData }

        if (missing.isNotEmpty()) {
            return ImportResult(
                imported = emptyMap(),
                errors = listOf("CVM fund data not found for CNPJs: ${missing.map { it.cnpj }}")
            )
        }

        val imported = funds.associateWith { saveAllQuotaValues(cvmFundData.getValue(it.cnpj)) }

        return ImportResult(imported, errors = emptyList())
    }

    private fun saveAllQuotaValues(
        cvmFundDataList: List<CvmFundData>
    ): List<ImportedQuota> {

        return cvmFundDataList
            .map { it -> saveQuotaValue(it) }
            .toList()

    }

    private fun saveQuotaValue(
        cvmFundData: CvmFundData
    ): ImportedQuota {

        val id = InvestmentFundDailyInfoId(
            cnpj = cvmFundData.cnpj,
            competenceDate = cvmFundData.date.fromYYYYMMDDToLocalDate()
        )
        val importedQuotaValue = cvmFundData.quotaValue.fromStringToBigDecimal(scale = 12)
        val storedQuotaValue = investmentFundDailyInfoRepository.findById(id).orElse(null)?.quotaValue

        val quota = InvestmentFundDailyInfo().apply {
            this.id = id
            fundClass = cvmFundData.fundType
            quotaValue = importedQuotaValue
        }

        investmentFundDailyInfoRepository.save(quota)

        return ImportedQuota(
            cnpj = id.cnpj,
            competenceDate = id.competenceDate,
            quotaValue = importedQuotaValue,
            replacedQuotaValue = storedQuotaValue?.takeIf { it.compareTo(importedQuotaValue) != 0 }
        )
    }

}

data class ImportedQuota(
    val cnpj: String,
    val competenceDate: LocalDate,
    val quotaValue: BigDecimal,
    /** Set only when this import changed an existing value, so corrections are visible. */
    val replacedQuotaValue: BigDecimal?
)

data class ImportResult(
    val imported: Map<FundSummary, List<ImportedQuota>>,
    val errors: List<String>
)