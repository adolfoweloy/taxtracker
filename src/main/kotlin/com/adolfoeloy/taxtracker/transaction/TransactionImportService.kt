package com.adolfoeloy.taxtracker.transaction

import com.adolfoeloy.taxtracker.balance.BalanceRequest
import com.opencsv.bean.CsvToBeanBuilder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStreamReader
import java.time.format.DateTimeFormatter

@Service
class TransactionImportService {

    fun processCsvFile(filePath: MultipartFile) {
        // Validate file
        if (filePath.isEmpty) {
            throw IllegalArgumentException("File is empty")
        }

        // Validate file type
        if (filePath.contentType != "text/csv"
            && !filePath.originalFilename?.endsWith(".csv", ignoreCase = true)!!) {
            throw IllegalArgumentException("Only CSV files are allowed")
        }

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val result = readCsvWithOpenCsv(filePath)

        result.forEach { transactionRequest ->
            // Here you would typically process each transactionRequest
            // For example, saving it to a database or performing some calculations
            println("Processing transaction: $transactionRequest")
        }

    }

    private fun readCsvWithOpenCsv(filePath: MultipartFile): List<TransactionRequest> {
        return CsvToBeanBuilder<TransactionRequest>(InputStreamReader(filePath.inputStream))
            .withType(TransactionRequest::class.java)
            .withSeparator(';')
            .build()
            .parse()
    }
}