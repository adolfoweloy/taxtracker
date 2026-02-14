package com.adolfoeloy.taxtracker.transaction

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/import/transaction")
class TransactionImportPageController(
    private val transactionImportService: TransactionImportService
) {

    @GetMapping
    fun transactionImportPage(): String {
        return "transaction_import"
    }

    @PostMapping
    fun processTransactionImport(
        @RequestParam("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            val rowsProcessed = transactionImportService.processCsvFile(file)
            redirectAttributes.addFlashAttribute(
                "successMessage",
                "CSV file processed successfully ($rowsProcessed rows)")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Failed to process CSV file: ${e.message}")
        }
        return "redirect:/import/transaction"
    }
}