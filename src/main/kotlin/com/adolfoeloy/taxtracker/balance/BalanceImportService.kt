package com.adolfoeloy.taxtracker.balance

import com.adolfoeloy.taxtracker.product.Product
import com.adolfoeloy.taxtracker.product.ProductRepository
import com.adolfoeloy.taxtracker.product.Products
import com.opencsv.bean.CsvToBeanBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class BalanceImportService(
    private val balanceRepository: BalanceRepository,
    private val productRepository: ProductRepository
) {

    @Transactional
    fun processCsvFile(filePath: MultipartFile): Int {
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
        var rowsProcessed = 0

        result.forEach { balanceRequest ->
            val product = productRepository
                .findByCertificate(balanceRequest.certificate) ?:
                productRepository.save<Product>(
                    Products.Companion.createProduct(
                    name = balanceRequest.product,
                    certificate = balanceRequest.certificate,
                    issuedAt = balanceRequest.issuedAt,
                    matureAt = balanceRequest.matureAt
                ))

            val balance = balanceRepository.findByProductCertificateAndBalanceAt(
                certificate = balanceRequest.certificate,
                balanceDate = LocalDate.parse(balanceRequest.balanceDate, formatter)
            )

            if (balance == null) {
                balanceRepository.save(
                    Balance().apply {
                        this.productId = product.id
                        this.percent = balanceRequest.percentage.toCents()
                        this.principal = balanceRequest.principal.toCents()
                        this.balance = balanceRequest.balance.toCents()
                        this.interest = balanceRequest.interest.toCents()
                        this.iof = balanceRequest.iof.toCents()
                        this.brTax = balanceRequest.brTax.toCents()
                        this.balanceNet = balanceRequest.balanceNet.toCents()
                        this.balanceDate = LocalDate.parse(balanceRequest.balanceDate, formatter)
                        this.brAuForex = balanceRequest.brToAuForex.toCents()
                    }
                )
            }

            rowsProcessed++
        }
        return rowsProcessed
    }

    private fun readCsvWithOpenCsv(filePath: MultipartFile): List<BalanceRequest> {
        return CsvToBeanBuilder<BalanceRequest>(InputStreamReader(filePath.inputStream))
            .withType(BalanceRequest::class.java)
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