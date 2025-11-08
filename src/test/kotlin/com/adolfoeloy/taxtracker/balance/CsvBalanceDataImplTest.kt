package com.adolfoeloy.taxtracker.balance

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class CsvBalanceDataImplTest {

    private val subject = CsvBalanceDataImpl()

    @Test
    fun `CSV file from CVM should load correct CvmFundData properties for Bradesco VGBL TRUXT Macro`() {
        val resourcePath = "/balance_test.csv"
        val file = File(this::class.java.getResource(resourcePath)?.file
            ?: throw IllegalStateException("Resource file not found"))

        val balanceListResult = subject.loadFrom(file.inputStream()).filter { it.product.isNotEmpty() }

        val expected = listOf(
            BalanceRequest(
                product = "CDB Fácil Bradesco",
                certificate = "1122",
                issuedAt = "19/11/2021",
                matureAt = "04/11/2024",
                percentage = "100.0000",
                principal = "1000.06",
                balance = "1200.14",
                interest = "80.08",
                iof = "0.00",
                brTax = "12.03",
                balanceNet = "101.11",
                balanceDate = "31/01/2023",
                brToAuForex = "0.25"
            ),
            BalanceRequest(
                product = "CDB Fácil Bradesco",
                certificate = "4455",
                issuedAt = "16/05/2022",
                matureAt = "30/04/2025",
                percentage = "100.0000",
                principal = "422.96",
                balance = "904.17",
                interest = "61.21",
                iof = "0.00",
                brTax = "96.24",
                balanceNet = "607.93",
                balanceDate = "31/01/2023",
                brToAuForex = "0.25"
            )
        )

        assertThat(balanceListResult)
            .hasSize(2)
            .containsExactlyElementsOf(expected)
    }

}