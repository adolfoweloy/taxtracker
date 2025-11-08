package com.adolfoeloy.taxtracker.vgbl

import org.springframework.web.bind.annotation.PostMapping
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

    @PostMapping("/import")
    fun importCvmFundData(
        @RequestParam("cnpj") cnpj: String,
        @RequestParam("file") file: MultipartFile
    ): VGBLImportResponse {
        val cvmFundData = csvCvmFundData.loadFrom(cnpj, file.inputStream)
        val vgblQuota = vgblFundService.saveQuotaValue(cvmFundData)
        return VGBLImportResponse(
            cnpj = vgblQuota.id.cnpj,
            fundType = vgblQuota.fundClass,
            quotaValue = vgblQuota.quotaValue.toString(),
            competenceDate = vgblQuota.id.competenceDate.toString()
        )
    }

    data class VGBLImportResponse(
        val cnpj: String,
        val fundType: String,
        val quotaValue: String,
        val competenceDate: String
    )
}