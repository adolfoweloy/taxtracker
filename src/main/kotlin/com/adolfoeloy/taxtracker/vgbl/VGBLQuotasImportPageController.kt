package com.adolfoeloy.taxtracker.vgbl

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/import/vgbl-quotas")
class VGBLQuotasImportPageController(
    private val vgblFundRepository: VGBLFundRepository,
    private val vgblQuotaRepository: VGBLQuotaRepository,
    private val csvCvmFundData: CsvCvmFundData,
    private val vgblFundService: VGBLFundService
) {

    @GetMapping
    fun importPage(model: Model): String {
        model.addAttribute("funds", vgblFundRepository.findAll())
        val quotasByFund = vgblQuotaRepository.findAllWithFundName()
            .groupBy { it.fundName }
            .entries.toList()
        model.addAttribute("quotaGroups", quotasByFund)
        return "vgbl_quotas_import"
    }

    @PostMapping
    fun importFile(
        @RequestParam("cnpjs") cnpjs: String,
        @RequestParam("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes
    ): String {
        val cnpjsList = cnpjs.split(",").map { it.trim() }
        val imported = mutableListOf<ImportedQuota>()
        val errors = mutableListOf<String>()

        for (cnpj in cnpjsList) {
            val cvmFundData = csvCvmFundData.loadFrom(cnpj, file.inputStream)

            if (cvmFundData == null) {
                errors.add("CVM fund data not found for CNPJ: $cnpj")
            } else {
                val quota = vgblFundService.saveQuotaValue(cvmFundData)
                val fund = vgblFundRepository.findById(cnpj).orElse(null)
                imported.add(ImportedQuota(
                    fundName = fund?.fundName ?: cnpj,
                    cnpj = quota.id.cnpj,
                    competenceDate = quota.id.competenceDate.toString(),
                    quotaValue = quota.quotaValue.toString()
                ))
            }
        }

        redirectAttributes.addFlashAttribute("imported", imported)
        if (errors.isNotEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", errors.joinToString("; "))
        }

        return "redirect:/import/vgbl-quotas"
    }

    data class ImportedQuota(
        val fundName: String,
        val cnpj: String,
        val competenceDate: String,
        val quotaValue: String
    )
}
