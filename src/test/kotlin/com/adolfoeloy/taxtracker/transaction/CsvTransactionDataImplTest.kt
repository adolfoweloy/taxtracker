package com.adolfoeloy.taxtracker.transaction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class CsvTransactionDataImplTest {

    private val subject = CsvTransactionDataImpl()

    @Test
    fun `CSV file from CVM should load correct CvmFundData properties for Bradesco VGBL TRUXT Macro`() {
        val resourcePath = "/transactions_test.csv"
        val file = File(this::class.java.getResource(resourcePath)?.file
            ?: throw IllegalStateException("Resource file not found"))

        val transactionsListResult = subject.loadFrom(file.inputStream()).filter { it.product.isNotEmpty() }

        val expected = listOf(
            TransactionRequest(
                product = "CDB Baixa Automática",
                certificate = "12582370000001221001",
                issuedAt = "13/05/2025",
                matureAt = "02/05/2028",
                paymentAt = "23/09/2025",
                percentage = "50.0000",
                principal = "98.01",
                redemption = "100.57",
                interest = "2.56",
                iof = "0.00",
                brTax = "0.57",
                credit = "100.00",
                description = "",
                brToAuForex = "0.25"
            ),
            TransactionRequest(
                product = "CDB Baixa Automática",
                certificate = "12582370000001221001",
                issuedAt = "13/05/2025",
                matureAt = "02/05/2028",
                paymentAt = "03/09/2025",
                percentage = "50.0000",
                principal = "98.31",
                redemption = "100.49",
                interest = "2.18",
                iof = "0.00",
                brTax = "0.49",
                credit = "100.00",
                description = "",
                brToAuForex = "0.25"
            ),
            TransactionRequest(
                product = "CDB Baixa Automática",
                certificate = "12582370000001221001",
                issuedAt = "13/05/2025",
                matureAt = "02/05/2028",
                paymentAt = "10/09/2025",
                percentage = "50.0000",
                principal = "98.20",
                redemption = "100.51",
                interest = "2.31",
                iof = "0.00",
                brTax = "0.51",
                credit = "100.00",
                description = "",
                brToAuForex = "0.25"
            )
        )

        assertThat(transactionsListResult)
            .hasSize(3)
            .containsExactlyElementsOf(expected)
    }

}