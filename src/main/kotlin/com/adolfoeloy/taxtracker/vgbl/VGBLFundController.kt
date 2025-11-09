package com.adolfoeloy.taxtracker.vgbl

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/vgbl")
class VGBLFundController(
    val csvCvmFundData: CsvCvmFundData,
    val vgblFundService: VGBLFundService
) {

    @PostMapping
    fun createFund(@RequestBody vgblFund: VGBLFundRequest): ResponseEntity<VGBLFundResponse> {
        val response = vgblFundService.saveFund(vgblFund)

        return ResponseEntity.ok(VGBLFundResponse(
            cnpj = response.cnpj,
            planName = response.planName,
            fundName = response.fundName,
            quotas = response.quotas.toString()
        ))
    }

    @GetMapping
    fun readFundsIncome(@RequestBody vgblFundRequest: VGBLFundIncomesRequest): List<VGBLMonthIncome> {

        val incomeDataForPeriod = vgblFundService.getIncomeDataForPeriod(
            cnpj = vgblFundRequest.cnpj,
            year = vgblFundRequest.year,
            startMonth = vgblFundRequest.startMonth,
            endMonth = vgblFundRequest.endMonth,
            currency = vgblFundRequest.currency ?: "BRL"
        )

        return incomeDataForPeriod
    }

    @PostMapping("/import")
    fun importCvmFundData(
        @RequestParam("cnpj") cnpj: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<VGBLImportResponse> {
        val cvmFundData = csvCvmFundData.loadFrom(cnpj, file.inputStream)

        if (cvmFundData == null) {
            return ResponseEntity.notFound().build<VGBLImportResponse>()
        }

        val vgblQuota = vgblFundService.saveQuotaValue(cvmFundData)

        val response = VGBLImportResponse(
            cnpj = vgblQuota.id.cnpj,
            fundType = vgblQuota.fundClass,
            quotaValue = vgblQuota.quotaValue.toString(),
            competenceDate = vgblQuota.id.competenceDate.toString()
        )

        return ResponseEntity.ok(response)
    }

    data class VGBLFundIncomesRequest(
        val cnpj: String,
        val year: Int,
        val startMonth: Int,
        val endMonth: Int,
        val currency: String?
    )

    data class VGBLImportResponse(
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