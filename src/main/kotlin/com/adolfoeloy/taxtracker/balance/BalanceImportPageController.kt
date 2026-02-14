package com.adolfoeloy.taxtracker.balance

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/import/balance")
class BalanceImportPageController(
    private val balanceImportService: BalanceImportService
) {

    @GetMapping
    fun importPage(): String {
        return "balance_import"
    }

    @PostMapping
    fun importFile(
        @RequestParam("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            val rowsProcessed = balanceImportService.processCsvFile(file)
            redirectAttributes.addFlashAttribute("successMessage", "CSV file processed successfully ($rowsProcessed rows)")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to process CSV file: ${e.message}")
        }
        return "redirect:/import/balance"
    }
}
