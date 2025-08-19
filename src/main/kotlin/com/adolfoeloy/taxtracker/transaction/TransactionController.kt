package com.adolfoeloy.taxtracker.transaction

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/transaction")
class TransactionController(
    private val transactionImportService: TransactionImportService
) {

    @PostMapping("/import")
    fun importTransactions(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, Any>> {

        try {
            val rowsProcessed = transactionImportService.processCsvFile(file)
            return ResponseEntity.ok(mapOf(
                "message" to "CSV file processed successfully",
                "rowsProcessed" to rowsProcessed,
            ))
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().body(mapOf<String, Any>(
                "error" to "Failed to process CSV file: ${e.message}"
            ))
        }
    }

}