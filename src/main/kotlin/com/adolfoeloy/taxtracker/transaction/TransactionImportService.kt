package com.adolfoeloy.taxtracker.transaction

import com.adolfoeloy.taxtracker.balance.BalanceRequest
import com.adolfoeloy.taxtracker.product.Certificate
import com.adolfoeloy.taxtracker.product.Product
import com.adolfoeloy.taxtracker.product.ProductRepository
import com.adolfoeloy.taxtracker.product.Products
import com.opencsv.bean.CsvToBeanBuilder
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class TransactionImportService(
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository
) {

    fun processCsvFile(filePath: MultipartFile): Int {
        var rowsProcessed = 0

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
            val certificate = Certificate.createNormalizedCertificate(transactionRequest.certificate)
            val product = productRepository
                .findByCertificate(certificate) ?:
            productRepository.save<Product>(
                Products.Companion.createProduct(
                    name = transactionRequest.product,
                    certificate = certificate,
                    issuedAt = transactionRequest.issuedAt,
                    matureAt = transactionRequest.matureAt
                ))

            transactionRepository.save(
                Transaction().apply {
                    this.productId = product.id
                    this.percent = transactionRequest.percentage.toCents()
                    this.principal = transactionRequest.principal.toCents()
                    this.paymentDate = transactionRequest.paymentAt
                        .let { LocalDate.parse(it, formatter) }
                    this.redemption = transactionRequest.redemption.toCents()
                    this.interest = transactionRequest.interest.toCents()
                    this.iof = transactionRequest.iof.toCents()
                    this.brTax = transactionRequest.brTax.toCents()
                    this.credited = transactionRequest.credit.toCents()
                    this.description = transactionRequest.description
                    this.brToAuForex = transactionRequest.brToAuForex.toCents()
                }
            )

            rowsProcessed++
        }

        return rowsProcessed
    }

    private fun readCsvWithOpenCsv(filePath: MultipartFile): List<TransactionRequest> {
        return CsvToBeanBuilder<TransactionRequest>(InputStreamReader(filePath.inputStream))
            .withType(TransactionRequest::class.java)
            .withSeparator(';')
            .build()
            .parse()
    }

    private fun String.toCents(): Int {
        val parts = this.split(".")
        if (parts.size > 2) {
            throw IllegalArgumentException("Invalid decimal format: $this")
        }
        val formatted = if (parts.size == 1) {
            "$this.00"
        } else {
            this
        }
        return formatted.replace(".", "").toInt()
    }
}