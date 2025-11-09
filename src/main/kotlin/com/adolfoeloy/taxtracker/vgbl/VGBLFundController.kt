package com.adolfoeloy.taxtracker.vgbl

import com.adolfoeloy.taxtracker.util.fromYearMonthString
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal
import java.math.RoundingMode

@RestController
@RequestMapping("/vgbl")
class VGBLFundController(
    val csvCvmFundData: CsvCvmFundData,
    val vgblFundService: VGBLFundService
) {

    @PostMapping
    fun save(@RequestBody vgblFund: VGBLFundRequest): ResponseEntity<VGBLFundResponse> {
        val response = vgblFundService.saveFund(vgblFund)

        return ResponseEntity.ok(VGBLFundResponse(
            cnpj = response.cnpj,
            planName = response.planName,
            fundName = response.fundName,
            quotas = response.quotas.toString()
        ))
    }

    @GetMapping
    fun search(@RequestBody vgblFundRequest: VGBLFundIncomesRequest): List<VGBLMonthIncome> {

        val incomeDataForPeriod = vgblFundService.getIncomeDataForPeriod(
            cnpj = vgblFundRequest.cnpj,
            start = vgblFundRequest.startYearMonth.fromYearMonthString(),
            end = vgblFundRequest.endYearMonth.fromYearMonthString(),
            currency = vgblFundRequest.currency ?: "BRL"
        )

        return incomeDataForPeriod
    }

    @GetMapping("/summary")
    fun searchSummary(@RequestBody vgblFundRequest: VGBLFundIncomesRequest): VGBLIncomeSummary {

        val incomeDataForPeriod = vgblFundService.getIncomeDataForPeriod(
            cnpj = vgblFundRequest.cnpj,
            start = vgblFundRequest.startYearMonth.fromYearMonthString(),
            end = vgblFundRequest.endYearMonth.fromYearMonthString(),
            currency = vgblFundRequest.currency ?: "BRL"
        )

        val totalIncome = incomeDataForPeriod
            .map { it.incomeDifference }
            .fold(BigDecimal.ZERO) { acc, value -> acc.add(value ?: BigDecimal.ZERO) }
            .setScale(2, RoundingMode.HALF_EVEN)

        return VGBLIncomeSummary(
            cnpj = vgblFundRequest.cnpj,
            yearMonthStart = vgblFundRequest.startYearMonth,
            yearMonthEnd = vgblFundRequest.endYearMonth,
            totalIncome = totalIncome.toString()
        )
    }

    data class VGBLIncomeSummary(
        val cnpj: String,
        val yearMonthStart: String,
        val yearMonthEnd: String,
        val totalIncome: String
    )

    @PostMapping("/import")
    fun importCvmFundData(
        @RequestParam("cnpjs") cnpjs: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<VGBLImportResult> {

        val cnpjsList = cnpjs.split(",").map { it.trim() }
        val result = VGBLImportResult()

        for (cnpj in cnpjsList) {
            val cvmFundData = csvCvmFundData.loadFrom(cnpj, file.inputStream)

            if (cvmFundData == null) {
                result.errors.add(VGBLErrorResult(
                    error = "CVM fund data not found for CNPJ: $cnpj",
                    cnpj = cnpj
                ))
            } else {
                val vgblQuota = vgblFundService.saveQuotaValue(cvmFundData)
                result.processed.add(
                    VGBLImportResultItem(
                        cnpj = vgblQuota.id.cnpj,
                        fundType = vgblQuota.fundClass,
                        quotaValue = vgblQuota.quotaValue.toString(),
                        competenceDate = vgblQuota.id.competenceDate.toString()
                    )
                )
            }
        }

        return when {
            result.errors.isEmpty() -> ResponseEntity.ok(result) // 200 - all succeeded
            result.processed.isEmpty() -> ResponseEntity.badRequest().body(result) // 400 - all failed
            else -> ResponseEntity.status(207).body(result) // 207 - partial success
        }
    }

    data class VGBLFundIncomesRequest(
        val cnpj: String,
        val startYearMonth: String,
        val endYearMonth: String,
        val currency: String?
    )

    data class VGBLImportResult(
        val processed: MutableList<VGBLImportResultItem> = mutableListOf<VGBLImportResultItem>(),
        val errors: MutableList<VGBLErrorResult> = mutableListOf<VGBLErrorResult>()
    )

    data class VGBLErrorResult(
        val error: String,
        val cnpj: String
    )

    data class VGBLImportResultItem(
        val cnpj: String,
        val fundType: String,
        val quotaValue: String,
        val competenceDate: String
    )

    data class VGBLFundResponse(
        val cnpj: String,
        val planName: String,
        val fundName: String,
        val quotas: String
    )
}