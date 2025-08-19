package com.adolfoeloy.taxtracker.balance

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/balance")
class BalanceController(
    private val balanceImportService: BalanceImportService
) {

    @PostMapping("/import")
    fun importBalance(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, Any>> {

        try {
            val rowsProcessed = balanceImportService.processCsvFile(file)

            return ResponseEntity.ok(mapOf<String, Any>(
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
